package com.shbudget.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookJoinRequest(
        @NotBlank(message = "초대 코드는 필수입니다.")
        @Size(min = 6, max = 6, message = "초대 코드는 6자리여야 합니다.")
        @Pattern(regexp = "^[A-Z0-9]{6}$", message = "초대 코드는 영문 대문자와 숫자만 가능합니다.")
        String inviteCode
) {
}
