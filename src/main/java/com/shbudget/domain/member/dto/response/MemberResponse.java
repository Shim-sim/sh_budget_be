package com.shbudget.domain.member.dto.response;

import com.shbudget.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import lombok.Builder;

@Schema(description = "회원 정보 응답")
@Builder
public record MemberResponse(
        @Schema(description = "회원 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl
) {
    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
