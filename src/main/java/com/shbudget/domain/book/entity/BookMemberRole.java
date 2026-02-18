package com.shbudget.domain.book.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookMemberRole {
    OWNER("가계부 소유자"),
    MEMBER("가계부 멤버");

    private final String description;
}
