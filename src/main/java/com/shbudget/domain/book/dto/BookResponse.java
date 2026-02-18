package com.shbudget.domain.book.dto;

import com.shbudget.domain.book.entity.Book;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookResponse(
        Long id,
        String name,
        String inviteCode,
        Long ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .inviteCode(book.getInviteCode())
                .ownerId(book.getOwnerId())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
