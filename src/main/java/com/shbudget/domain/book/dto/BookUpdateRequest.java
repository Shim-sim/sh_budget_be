package com.shbudget.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookUpdateRequest(
        @NotBlank(message = "가계부 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "가계부 이름은 1자 이상 50자 이하여야 합니다.")
        String name
) {
}
