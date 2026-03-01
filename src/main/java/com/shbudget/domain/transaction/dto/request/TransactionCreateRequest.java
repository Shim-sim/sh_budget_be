package com.shbudget.domain.transaction.dto.request;

import com.shbudget.domain.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TransactionCreateRequest(
        @NotNull(message = "가계부 ID는 필수입니다.")
        Long bookId,

        @NotNull(message = "거래 타입은 필수입니다.")
        TransactionType type,

        // INCOME/EXPENSE용 필드
        Long assetId,
        Long categoryId,

        // TRANSFER용 필드
        Long fromAssetId,
        Long toAssetId,

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        Long amount,

        @NotNull(message = "거래 날짜는 필수입니다.")
        LocalDate date,

        String memo
) {
}
