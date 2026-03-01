package com.shbudget.domain.transaction.entity;

import com.shbudget.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_book_date", columnList = "book_id, date"),
        @Index(name = "idx_book_type", columnList = "book_id, type"),
        @Index(name = "idx_created_by", columnList = "created_by")
})
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "book_id")
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_transaction_book"))
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private Long amount;

    // 수입/지출용 필드
    @Column(name = "asset_id")
    @JoinColumn(name = "asset_id", foreignKey = @ForeignKey(name = "fk_transaction_asset"))
    private Long assetId;

    @Column(name = "category_id")
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_transaction_category"))
    private Long categoryId;

    // 이체용 필드
    @Column(name = "from_asset_id")
    @JoinColumn(name = "from_asset_id", foreignKey = @ForeignKey(name = "fk_transaction_from_asset"))
    private Long fromAssetId;

    @Column(name = "to_asset_id")
    @JoinColumn(name = "to_asset_id", foreignKey = @ForeignKey(name = "fk_transaction_to_asset"))
    private Long toAssetId;

    // 공통 필드
    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String memo;

    @Column(nullable = false, name = "created_by")
    @JoinColumn(name = "created_by", foreignKey = @ForeignKey(name = "fk_transaction_creator"))
    private Long createdBy;

    //INCOME
    public static Transaction createIncome(Long bookId, Long assetId, Long categoryId,
                                            Long amount, LocalDate date, String memo, Long createdBy) {
        return Transaction.builder()
                .bookId(bookId)
                .type(TransactionType.INCOME)
                .assetId(assetId)
                .categoryId(categoryId)
                .amount(amount)
                .date(date)
                .memo(memo)
                .createdBy(createdBy)
                .build();
    }

    //EXPENSE
    public static Transaction createExpense(Long bookId, Long assetId, Long categoryId,
                                             Long amount, LocalDate date, String memo, Long createdBy) {
        return Transaction.builder()
                .bookId(bookId)
                .type(TransactionType.EXPENSE)
                .assetId(assetId)
                .categoryId(categoryId)
                .amount(amount)
                .date(date)
                .memo(memo)
                .createdBy(createdBy)
                .build();
    }

    // 정적 팩토리 메서드 - TRANSFER
    public static Transaction createTransfer(Long bookId, Long fromAssetId, Long toAssetId,
                                              Long amount, LocalDate date, String memo, Long createdBy) {
        return Transaction.builder()
                .bookId(bookId)
                .type(TransactionType.TRANSFER)
                .fromAssetId(fromAssetId)
                .toAssetId(toAssetId)
                .amount(amount)
                .date(date)
                .memo(memo)
                .createdBy(createdBy)
                .build();
    }

    // 거래 수정
    public void updateTransaction(Long assetId, Long categoryId, Long amount, LocalDate date, String memo) {
        // INCOME/EXPENSE 타입만 수정 가능
        if (this.type == TransactionType.INCOME || this.type == TransactionType.EXPENSE) {
            if (assetId != null) {
                this.assetId = assetId;
            }
            this.categoryId = categoryId;  // null 가능
        }

        if (amount != null) {
            this.amount = amount;
        }
        if (date != null) {
            this.date = date;
        }
        this.memo = memo;
    }

    // 이체 거래 수정
    public void updateTransfer(Long fromAssetId, Long toAssetId, Long amount, LocalDate date, String memo) {
        if (this.type != TransactionType.TRANSFER) {
            return;
        }

        if (fromAssetId != null) {
            this.fromAssetId = fromAssetId;
        }
        if (toAssetId != null) {
            this.toAssetId = toAssetId;
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (date != null) {
            this.date = date;
        }
        this.memo = memo;
    }
}
