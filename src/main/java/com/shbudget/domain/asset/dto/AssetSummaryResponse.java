package com.shbudget.domain.asset.dto;

import lombok.Builder;

@Builder
public record AssetSummaryResponse(
        Long totalBalance,
        Integer assetCount
) {
    public static AssetSummaryResponse of(Long totalBalance, Integer assetCount) {
        return AssetSummaryResponse.builder()
                .totalBalance(totalBalance != null ? totalBalance : 0L)
                .assetCount(assetCount)
                .build();
    }
}
