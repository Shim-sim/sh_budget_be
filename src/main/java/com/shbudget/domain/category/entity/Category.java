package com.shbudget.domain.category.entity;

import com.shbudget.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_book_id", columnList = "book_id")
})
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "book_id")
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_category_book"))
    private Long bookId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;  // 카테고리 색상 (예: #FF5733)

    @Column(length = 50)
    private String icon;   // 아이콘 이름 또는 URL

    // 정적 팩토리 메서드
    public static Category create(Long bookId, String name, String color, String icon) {
        return Category.builder()
                .bookId(bookId)
                .name(name)
                .color(color)
                .icon(icon)
                .build();
    }

    public static Category create(Long bookId, String name) {
        return Category.builder()
                .bookId(bookId)
                .name(name)
                .build();
    }

    // 카테고리 수정
    public void updateCategory(String name, String color, String icon) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.color = color;
        this.icon = icon;
    }
}
