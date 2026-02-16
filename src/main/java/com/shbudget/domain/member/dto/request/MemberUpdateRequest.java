package com.shbudget.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 정보 수정 요청")
public record MemberUpdateRequest(
        @Schema(description = "닉네임", example = "홍길동")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl
) {
}
