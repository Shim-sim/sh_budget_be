package com.shbudget.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "인증 응답 (JWT 토큰)")
@Builder
public record AuthResponse(
        @Schema(description = "액세스 토큰")
        String accessToken,

        @Schema(description = "리프레시 토큰")
        String refreshToken,

        @Schema(description = "회원 ID", example = "1")
        Long memberId
) {
}
