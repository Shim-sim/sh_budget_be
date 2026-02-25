package com.shbudget.domain.transaction.dto.response;

import com.shbudget.domain.transaction.entity.Transaction;
import com.shbudget.domain.transaction.entity.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long bookId,
        TransactionType type,

        // INCOME/EXPENSE용 필드
        Long assetId,
        String assetName,
        Long categoryId,
        String categoryName,

        // TRANSFER용 필드
        Long fromAssetId,
        String fromAssetName,
        Long toAssetId,
        String toAssetName,

        // 공통 필드
        Long amount,
        LocalDate date,
        String memo,
        Long createdBy,
        String createdByNickname,
        LocalDateTime createdAt
) {
    // 기본 변환 (이름 정보 없음)
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getBookId(),
                transaction.getType(),
                transaction.getAssetId(),
                null,
                transaction.getCategoryId(),
                null,
                transaction.getFromAssetId(),
                null,
                transaction.getToAssetId(),
                null,
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getMemo(),
                transaction.getCreatedBy(),
                null,
                transaction.getCreatedAt()
        );
    }

    // INCOME/EXPENSE용 변환 (이름 포함)
    public static TransactionResponse from(Transaction transaction, String assetName,
                                            String categoryName, String createdByNickname) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getBookId(),
                transaction.getType(),
                transaction.getAssetId(),
                assetName,
                transaction.getCategoryId(),
                categoryName,
                null,
                null,
                null,
                null,
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getMemo(),
                transaction.getCreatedBy(),
                createdByNickname,
                transaction.getCreatedAt()
        );
    }

    // TRANSFER용 변환 (이름 포함)
    public static TransactionResponse fromTransfer(Transaction transaction, String fromAssetName,
                                                     String toAssetName, String createdByNickname) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getBookId(),
                transaction.getType(),
                null,
                null,
                null,
                null,
                transaction.getFromAssetId(),
                fromAssetName,
                transaction.getToAssetId(),
                toAssetName,
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getMemo(),
                transaction.getCreatedBy(),
                createdByNickname,
                transaction.getCreatedAt()
        );
    }
}
