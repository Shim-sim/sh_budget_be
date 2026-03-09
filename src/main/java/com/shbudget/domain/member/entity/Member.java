package com.shbudget.domain.member.entity;

import com.shbudget.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "members")
@SuperBuilder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    private Member(String email, String password, String nickname, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public static Member create(String email, String nickname) {
        return Member.builder()
            .email(email)
            .nickname(nickname)
            .build();
    }

    public static Member createWithPassword(String email, String password, String nickname) {
        return Member.builder()
            .email(email)
            .password(password)
            .nickname(nickname)
            .build();
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
