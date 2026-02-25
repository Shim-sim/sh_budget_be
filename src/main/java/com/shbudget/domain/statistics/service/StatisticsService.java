package com.shbudget.domain.statistics.service;

import com.shbudget.domain.asset.repository.AssetRepository;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.category.entity.Category;
import com.shbudget.domain.category.repository.CategoryRepository;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.domain.statistics.dto.response.*;
import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.domain.transaction.repository.TransactionRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final BookMemberRepository bookMemberRepository;

    /**
     * 월별 수입/지출 요약
     */
    public MonthlySummaryResponse getMonthlySummary(Long memberId, Long bookId, Integer year, Integer month) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        // 기간 설정
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 수입/지출 합계
        Long totalIncome = transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                bookId, TransactionType.INCOME, startDate, endDate
        );

        Long totalExpense = transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                bookId, TransactionType.EXPENSE, startDate, endDate
        );

        // 총 자산 (현재 시점)
        Long totalAssets = assetRepository.sumBalanceByBookId(bookId);
        if (totalAssets == null) {
            totalAssets = 0L;
        }

        return MonthlySummaryResponse.of(year, month, totalIncome, totalExpense, totalAssets);
    }

    /**
     * 카테고리별 지출 통계
     */
    public CategoryStatisticsResponse getCategoryStatistics(Long memberId, Long bookId,
                                                             Integer year, Integer month) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        // 기간 설정
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 전체 지출
        Long totalExpense = transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                bookId, TransactionType.EXPENSE, startDate, endDate
        );

        // 카테고리별 지출 (categoryId, amount, count)
        List<Object[]> categoryData = transactionRepository.sumAmountByCategoryAndDateBetween(
                bookId, TransactionType.EXPENSE, startDate, endDate
        );

        // 카테고리 정보 조회
        Map<Long, Category> categoryMap = categoryRepository.findAllByBookIdOrderByCreatedAtAsc(bookId)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // CategoryStatistics 변환
        List<CategoryStatistics> categories = categoryData.stream()
                .map(data -> {
                    Long categoryId = (Long) data[0];
                    Long amount = (Long) data[1];
                    Long count = (Long) data[2];
                    Category category = categoryMap.get(categoryId);
                    String categoryName = category != null ? category.getName() : "미분류";

                    // 비율 계산
                    double percentage = totalExpense > 0
                            ? (amount * 100.0 / totalExpense)
                            : 0.0;

                    return new CategoryStatistics(
                            categoryId,
                            categoryName,
                            amount,
                            count.intValue(),
                            percentage
                    );
                })
                .sorted((a, b) -> Long.compare(b.totalAmount(), a.totalAmount()))  // 금액 내림차순
                .collect(Collectors.toList());

        return new CategoryStatisticsResponse(year, month, totalExpense, categories);
    }

    /**
     * 멤버별 기여도 통계
     */
    public List<MemberContributionResponse> getMemberContribution(Long memberId, Long bookId,
                                                                    Integer year, Integer month) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        // 기간 설정
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 전체 수입/지출
        Long totalIncome = transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                bookId, TransactionType.INCOME, startDate, endDate
        );

        Long totalExpense = transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                bookId, TransactionType.EXPENSE, startDate, endDate
        );

        // 멤버별 수입 (memberId, amount)
        List<Object[]> incomeData = transactionRepository.sumAmountByMemberAndTypeAndDateBetween(
                bookId, TransactionType.INCOME, startDate, endDate
        );

        // 멤버별 지출 (memberId, amount)
        List<Object[]> expenseData = transactionRepository.sumAmountByMemberAndTypeAndDateBetween(
                bookId, TransactionType.EXPENSE, startDate, endDate
        );

        // Map으로 변환
        Map<Long, Long> incomeMap = incomeData.stream()
                .collect(Collectors.toMap(
                        data -> (Long) data[0],
                        data -> (Long) data[1]
                ));

        Map<Long, Long> expenseMap = expenseData.stream()
                .collect(Collectors.toMap(
                        data -> (Long) data[0],
                        data -> (Long) data[1]
                ));

        // 모든 멤버 ID 수집
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.addAll(incomeMap.keySet());
        expenseMap.keySet().forEach(id -> {
            if (!allMemberIds.contains(id)) {
                allMemberIds.add(id);
            }
        });

        // Member 정보 조회
        Map<Long, Member> memberMap = memberRepository.findAllById(allMemberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        // MemberContributionResponse 변환
        return allMemberIds.stream()
                .map(memberIdItem -> {
                    Member member = memberMap.get(memberIdItem);
                    String nickname = member != null ? member.getNickname() : "알 수 없음";

                    Long memberIncome = incomeMap.getOrDefault(memberIdItem, 0L);
                    Long memberExpense = expenseMap.getOrDefault(memberIdItem, 0L);

                    // 비율 계산
                    double incomePercentage = totalIncome > 0
                            ? (memberIncome * 100.0 / totalIncome)
                            : 0.0;

                    double expensePercentage = totalExpense > 0
                            ? (memberExpense * 100.0 / totalExpense)
                            : 0.0;

                    return new MemberContributionResponse(
                            memberIdItem,
                            nickname,
                            memberIncome,
                            memberExpense,
                            incomePercentage,
                            expensePercentage
                    );
                })
                .sorted((a, b) -> Long.compare(b.totalIncome(), a.totalIncome()))  // 수입 내림차순
                .collect(Collectors.toList());
    }

    // === Private Helper Methods ===

    private void validateBookMember(Long bookId, Long memberId) {
        if (!bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new CustomException(ErrorCode.NOT_BOOK_MEMBER);
        }
    }
}
