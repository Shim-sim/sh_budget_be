package com.shbudget.domain.transaction.dto.request;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TransactionUpdateRequest(
        // INCOME/EXPENSE용 필드
        Long assetId,
        Long categoryId,

        // TRANSFER용 필드
        Long fromAssetId,
        Long toAssetId,

        @Positive(message = "금액은 0보다 커야 합니다.")
        Long amount,

        LocalDate date,

        String memo
) {
}
