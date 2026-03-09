package com.shbudget.domain.recurring.dto.response;

import com.shbudget.domain.recurring.entity.RecurringTransaction;
import com.shbudget.domain.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "반복 거래 응답")
@Builder
public record RecurringResponse(
        Long id,
        Long bookId,
        TransactionType type,
        Long amount,
        Integer dayOfMonth,
        String memo,
        Long assetId,
        String assetName,
        Long categoryId,
        String categoryName,
        Long fromAssetId,
        String fromAssetName,
        Long toAssetId,
        String toAssetName,
        Long createdBy,
        String createdByNickname
) {
    public static RecurringResponse from(RecurringTransaction entity,
                                         String assetName,
                                         String categoryName,
                                         String fromAssetName,
                                         String toAssetName,
                                         String createdByNickname) {
        return RecurringResponse.builder()
                .id(entity.getId())
                .bookId(entity.getBookId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .dayOfMonth(entity.getDayOfMonth())
                .memo(entity.getMemo())
                .assetId(entity.getAssetId())
                .assetName(assetName)
                .categoryId(entity.getCategoryId())
                .categoryName(categoryName)
                .fromAssetId(entity.getFromAssetId())
                .fromAssetName(fromAssetName)
                .toAssetId(entity.getToAssetId())
                .toAssetName(toAssetName)
                .createdBy(entity.getCreatedBy())
                .createdByNickname(createdByNickname)
                .build();
    }
}
