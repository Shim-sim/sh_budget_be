package com.shbudget.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record RegisterRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 50, message = "비밀번호는 8자 이상 50자 이하여야 합니다.")
        String password,

        @Schema(description = "닉네임", example = "홍길동")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        String nickname
) {
}
