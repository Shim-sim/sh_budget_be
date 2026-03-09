package com.shbudget.domain.recurring.entity;

import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "recurring_transactions", indexes = {
        @Index(name = "idx_recurring_book", columnList = "book_id"),
        @Index(name = "idx_recurring_day", columnList = "day_of_month, active")
})
public class RecurringTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "book_id")
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private Long amount;

    /** 매월 반복일 (1~31) */
    @Column(nullable = false, name = "day_of_month")
    private Integer dayOfMonth;

    @Column(length = 500)
    private String memo;

    // INCOME/EXPENSE용
    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "category_id")
    private Long categoryId;

    // TRANSFER용
    @Column(name = "from_asset_id")
    private Long fromAssetId;

    @Column(name = "to_asset_id")
    private Long toAssetId;

    @Column(nullable = false, name = "created_by")
    private Long createdBy;

    @Column(nullable = false)
    private Boolean active;

    public void deactivate() {
        this.active = false;
    }
}
