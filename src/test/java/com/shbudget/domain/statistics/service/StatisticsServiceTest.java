package com.shbudget.domain.statistics.service;

import com.shbudget.domain.asset.repository.AssetRepository;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.category.entity.Category;
import com.shbudget.domain.category.repository.CategoryRepository;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.domain.statistics.dto.response.CategoryStatisticsResponse;
import com.shbudget.domain.statistics.dto.response.MemberContributionResponse;
import com.shbudget.domain.statistics.dto.response.MonthlySummaryResponse;
import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.domain.transaction.repository.TransactionRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookMemberRepository bookMemberRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    @DisplayName("월별 수입/지출 요약 조회 성공")
    void getMonthlySummary() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Integer year = 2024;
        Integer month = 5;

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(5000000L);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(3000000L);
        when(assetRepository.sumBalanceByBookId(bookId)).thenReturn(10000000L);

        // when
        MonthlySummaryResponse response = statisticsService.getMonthlySummary(memberId, bookId, year, month);

        // then
        assertThat(response.year()).isEqualTo(2024);
        assertThat(response.month()).isEqualTo(5);
        assertThat(response.totalIncome()).isEqualTo(5000000L);
        assertThat(response.totalExpense()).isEqualTo(3000000L);
        assertThat(response.netIncome()).isEqualTo(2000000L);  // 5000000 - 3000000
        assertThat(response.totalAssets()).isEqualTo(10000000L);
    }

    @Test
    @DisplayName("카테고리별 지출 통계 조회 성공")
    void getCategoryStatistics() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Integer year = 2024;
        Integer month = 5;

        Category category1 = Category.create(bookId, "식비", "#FF5733", "food");
        Category category2 = Category.create(bookId, "교통", "#3498DB", "transport");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1000000L);
        when(transactionRepository.sumAmountByCategoryAndDateBetween(
                eq(bookId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new Object[]{1L, 600000L, 10L},  // 식비: 60%
                        new Object[]{2L, 400000L, 5L}    // 교통: 40%
                ));
        when(categoryRepository.findAllByBookIdOrderByCreatedAtAsc(bookId))
                .thenReturn(List.of(category1, category2));

        // when
        CategoryStatisticsResponse response = statisticsService.getCategoryStatistics(memberId, bookId, year, month);

        // then
        assertThat(response.year()).isEqualTo(2024);
        assertThat(response.month()).isEqualTo(5);
        assertThat(response.totalExpense()).isEqualTo(1000000L);
        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories().get(0).totalAmount()).isEqualTo(600000L);
        assertThat(response.categories().get(0).percentage()).isEqualTo(60.0);
    }

    @Test
    @DisplayName("멤버별 기여도 통계 조회 성공")
    void getMemberContribution() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Integer year = 2024;
        Integer month = 5;

        Member member1 = Member.create("user1@test.com", "홍길동");
        Member member2 = Member.create("user2@test.com", "김철수");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(5000000L);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(3000000L);
        when(transactionRepository.sumAmountByMemberAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new Object[]{1L, 3000000L},  // 홍길동: 60%
                        new Object[]{2L, 2000000L}   // 김철수: 40%
                ));
        when(transactionRepository.sumAmountByMemberAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        new Object[]{1L, 1500000L},  // 홍길동: 50%
                        new Object[]{2L, 1500000L}   // 김철수: 50%
                ));
        when(memberRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(member1, member2));

        // when
        List<MemberContributionResponse> response = statisticsService.getMemberContribution(memberId, bookId, year, month);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).totalIncome()).isEqualTo(3000000L);
        assertThat(response.get(0).incomePercentage()).isEqualTo(60.0);
        assertThat(response.get(0).expensePercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("가계부 멤버가 아닌 경우 조회 실패")
    void getMonthlySummary_NotBookMember() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Integer year = 2024;
        Integer month = 5;

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> statisticsService.getMonthlySummary(memberId, bookId, year, month))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_MEMBER);
    }

    @Test
    @DisplayName("총 자산이 null인 경우 0으로 처리")
    void getMonthlySummary_NullAssets() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Integer year = 2024;
        Integer month = 5;

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1000000L);
        when(transactionRepository.sumAmountByBookIdAndTypeAndDateBetween(
                eq(bookId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(500000L);
        when(assetRepository.sumBalanceByBookId(bookId)).thenReturn(null);

        // when
        MonthlySummaryResponse response = statisticsService.getMonthlySummary(memberId, bookId, year, month);

        // then
        assertThat(response.totalAssets()).isEqualTo(0L);
    }
}
