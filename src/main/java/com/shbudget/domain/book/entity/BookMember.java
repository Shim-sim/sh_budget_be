package com.shbudget.domain.book.entity;

import com.shbudget.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "member_id"}))
public class BookMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "book_id")
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_book_member_book"))
    private Long bookId;

    @Column(nullable = false, name = "member_id")
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_book_member_member"))
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BookMemberRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private BookMember(Long bookId, Long memberId, BookMemberRole role) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public static BookMember createOwner(Long bookId, Long memberId) {
        return new BookMember(bookId, memberId, BookMemberRole.OWNER);
    }

    public static BookMember createMember(Long bookId, Long memberId) {
        return new BookMember(bookId, memberId, BookMemberRole.MEMBER);
    }

    public boolean isOwner() {
        return this.role == BookMemberRole.OWNER;
    }

    public boolean isMember() {
        return this.role == BookMemberRole.MEMBER;
    }
}
