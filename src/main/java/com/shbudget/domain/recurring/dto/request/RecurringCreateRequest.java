package com.shbudget.domain.recurring.dto.request;

import com.shbudget.domain.transaction.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "반복 거래 등록 요청")
public record RecurringCreateRequest(
        @Schema(description = "가계부 ID")
        @NotNull(message = "가계부 ID는 필수입니다.")
        Long bookId,

        @Schema(description = "거래 타입")
        @NotNull(message = "거래 타입은 필수입니다.")
        TransactionType type,

        @Schema(description = "금액")
        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        Long amount,

        @Schema(description = "매월 반복일 (1~31)", example = "15")
        @NotNull(message = "반복일은 필수입니다.")
        @Min(value = 1, message = "반복일은 1 이상이어야 합니다.")
        @Max(value = 31, message = "반복일은 31 이하여야 합니다.")
        Integer dayOfMonth,

        @Schema(description = "메모")
        String memo,

        // INCOME/EXPENSE
        Long assetId,
        Long categoryId,

        // TRANSFER
        Long fromAssetId,
        Long toAssetId
) {
}
