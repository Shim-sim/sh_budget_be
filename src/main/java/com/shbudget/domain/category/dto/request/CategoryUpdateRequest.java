package com.shbudget.domain.category.dto.request;

import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
        String name,

        @Size(max = 20, message = "색상은 20자 이하여야 합니다.")
        String color,

        @Size(max = 50, message = "아이콘은 50자 이하여야 합니다.")
        String icon
) {
}
