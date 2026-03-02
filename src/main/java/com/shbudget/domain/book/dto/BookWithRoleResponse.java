package com.shbudget.domain.book.dto;

import com.shbudget.domain.book.entity.Book;
import com.shbudget.domain.book.entity.BookMemberRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookWithRoleResponse(
        Long id,
        String name,
        String inviteCode,
        Long ownerId,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookWithRoleResponse from(Book book, BookMemberRole role) {
        return BookWithRoleResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .inviteCode(book.getInviteCode())
                .ownerId(book.getOwnerId())
                .role(role.name())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
