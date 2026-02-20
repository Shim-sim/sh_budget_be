package com.shbudget.domain.asset.entity;

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
@Table(name = "assets")
public class Asset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "book_id")
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_asset_book"))
    private Long bookId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long balance;

    @Column(name = "owner_member_id")
    @JoinColumn(name = "owner_member_id", foreignKey = @ForeignKey(name = "fk_asset_owner_member"))
    private Long ownerMemberId;

    public static Asset create(Long bookId, String name, Long balance) {
        return Asset.builder()
                .bookId(bookId)
                .name(name)
                .balance(balance)
                .build();
    }

    public static Asset create(Long bookId, String name, Long balance, Long ownerMemberId) {
        return Asset.builder()
                .bookId(bookId)
                .name(name)
                .balance(balance)
                .ownerMemberId(ownerMemberId)
                .build();
    }

    public void updateAsset(String name, Long balance, Long ownerMemberId) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (balance != null) {
            this.balance = balance;
        }
        this.ownerMemberId = ownerMemberId;
    }
}
