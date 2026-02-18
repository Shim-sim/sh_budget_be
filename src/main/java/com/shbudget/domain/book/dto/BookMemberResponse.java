package com.shbudget.domain.book.dto;

import com.shbudget.domain.book.entity.BookMember;
import com.shbudget.domain.book.entity.BookMemberRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookMemberResponse(
        Long id,
        Long bookId,
        Long memberId,
        BookMemberRole role,
        LocalDateTime joinedAt
) {
    public static BookMemberResponse from(BookMember bookMember) {
        return BookMemberResponse.builder()
                .id(bookMember.getId())
                .bookId(bookMember.getBookId())
                .memberId(bookMember.getMemberId())
                .role(bookMember.getRole())
                .joinedAt(bookMember.getJoinedAt())
                .build();
    }
}
