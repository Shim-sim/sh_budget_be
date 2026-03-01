package com.shbudget.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotNull(message = "가계부 ID는 필수입니다.")
        Long bookId,

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
        String name,

        @Size(max = 20, message = "색상은 20자 이하여야 합니다.")
        String color,

        @Size(max = 50, message = "아이콘은 50자 이하여야 합니다.")
        String icon
) {
}
