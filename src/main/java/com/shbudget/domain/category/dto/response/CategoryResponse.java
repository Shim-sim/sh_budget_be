package com.shbudget.domain.category.dto.response;

import com.shbudget.domain.category.entity.Category;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        Long bookId,
        String name,
        String color,
        String icon,
        LocalDateTime createdAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getBookId(),
                category.getName(),
                category.getColor(),
                category.getIcon(),
                category.getCreatedAt()
        );
    }
}
