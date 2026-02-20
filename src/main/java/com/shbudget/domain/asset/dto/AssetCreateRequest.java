package com.shbudget.domain.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetCreateRequest(
        @NotBlank(message = "자산 이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "자산 이름은 1자 이상 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "잔액은 필수입니다.")
        Long balance,

        Long ownerMemberId
) {
}
