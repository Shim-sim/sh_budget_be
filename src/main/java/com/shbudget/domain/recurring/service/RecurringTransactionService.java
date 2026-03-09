package com.shbudget.domain.recurring.service;

import com.shbudget.domain.asset.entity.Asset;
import com.shbudget.domain.asset.repository.AssetRepository;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.category.entity.Category;
import com.shbudget.domain.category.repository.CategoryRepository;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.domain.recurring.dto.request.RecurringCreateRequest;
import com.shbudget.domain.recurring.dto.response.RecurringResponse;
import com.shbudget.domain.recurring.entity.RecurringTransaction;
import com.shbudget.domain.recurring.repository.RecurringTransactionRepository;
import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepository;
    private final BookMemberRepository bookMemberRepository;
    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public RecurringResponse create(Long memberId, RecurringCreateRequest request) {
        validateBookMember(request.bookId(), memberId);

        // 타입별 필수값 검증
        if (request.type() == TransactionType.TRANSFER) {
            if (request.fromAssetId() == null || request.toAssetId() == null) {
                throw new CustomException(ErrorCode.ASSETS_REQUIRED_FOR_TRANSFER);
            }
        } else {
            if (request.assetId() == null) {
                throw new CustomException(ErrorCode.ASSET_REQUIRED_FOR_INCOME_EXPENSE);
            }
        }

        RecurringTransaction entity = RecurringTransaction.builder()
                .bookId(request.bookId())
                .type(request.type())
                .amount(request.amount())
                .dayOfMonth(request.dayOfMonth())
                .memo(request.memo())
                .assetId(request.assetId())
                .categoryId(request.categoryId())
                .fromAssetId(request.fromAssetId())
                .toAssetId(request.toAssetId())
                .createdBy(memberId)
                .active(true)
                .build();

        recurringRepository.save(entity);
        return buildResponse(entity);
    }

    public List<RecurringResponse> getList(Long memberId, Long bookId) {
        validateBookMember(bookId, memberId);
        return recurringRepository.findAllByBookIdAndActiveTrue(bookId).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long memberId, Long id) {
        RecurringTransaction entity = recurringRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RECURRING_NOT_FOUND));
        validateBookMember(entity.getBookId(), memberId);
        entity.deactivate();
    }

    private void validateBookMember(Long bookId, Long memberId) {
        if (!bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new CustomException(ErrorCode.NOT_BOOK_MEMBER);
        }
    }

    private RecurringResponse buildResponse(RecurringTransaction entity) {
        String assetName = entity.getAssetId() != null
                ? assetRepository.findById(entity.getAssetId()).map(Asset::getName).orElse(null)
                : null;
        String categoryName = entity.getCategoryId() != null
                ? categoryRepository.findById(entity.getCategoryId()).map(Category::getName).orElse(null)
                : null;
        String fromAssetName = entity.getFromAssetId() != null
                ? assetRepository.findById(entity.getFromAssetId()).map(Asset::getName).orElse(null)
                : null;
        String toAssetName = entity.getToAssetId() != null
                ? assetRepository.findById(entity.getToAssetId()).map(Asset::getName).orElse(null)
                : null;
        String createdByNickname = memberRepository.findById(entity.getCreatedBy())
                .map(Member::getNickname).orElse(null);

        return RecurringResponse.from(entity, assetName, categoryName, fromAssetName, toAssetName, createdByNickname);
    }
}
