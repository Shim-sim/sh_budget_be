package com.shbudget.domain.asset.dto;

import com.shbudget.domain.asset.entity.Asset;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AssetResponse(
        Long id,
        Long bookId,
        String name,
        Long balance,
        Long ownerMemberId,
        String ownerNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .bookId(asset.getBookId())
                .name(asset.getName())
                .balance(asset.getBalance())
                .ownerMemberId(asset.getOwnerMemberId())
                .ownerNickname(null)  // Service에서 채워짐
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }

    public static AssetResponse from(Asset asset, String ownerNickname) {
        return AssetResponse.builder()
                .id(asset.getId())
                .bookId(asset.getBookId())
                .name(asset.getName())
                .balance(asset.getBalance())
                .ownerMemberId(asset.getOwnerMemberId())
                .ownerNickname(ownerNickname)
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
