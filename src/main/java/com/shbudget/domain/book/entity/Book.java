package com.shbudget.domain.book.entity;

import com.shbudget.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 6)
    private String inviteCode;

    @Column(nullable = false, name = "owner_id")
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_book_owner"))
    private Long ownerId;

    private Book(String name, Long ownerId) {
        this.name = name;
        this.ownerId = ownerId;
        this.inviteCode = generateInviteCode();
    }

    public static Book create(String name, Long ownerId) {
        return new Book(name, ownerId);
    }

    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public void regenerateInviteCode() {
        this.inviteCode = generateInviteCode();
    }

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(INVITE_CODE_CHARS.length());
            code.append(INVITE_CODE_CHARS.charAt(index));
        }
        return code.toString();
    }
}
