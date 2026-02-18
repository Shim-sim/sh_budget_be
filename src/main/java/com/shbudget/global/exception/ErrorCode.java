package com.shbudget.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT(400, "잘못된 입력입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),

    // Member
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(409, "이미 존재하는 이메일입니다."),

    // Book
    BOOK_NOT_FOUND(404, "가계부를 찾을 수 없습니다."),
    INVALID_INVITE_CODE(400, "유효하지 않은 초대 코드입니다."),
    ALREADY_JOINED_BOOK(409, "이미 가계부에 참여 중입니다."),
    NOT_BOOK_MEMBER(403, "가계부 멤버가 아닙니다."),
    NOT_BOOK_OWNER(403, "가계부 소유자만 수행할 수 있습니다."),
    OWNER_CANNOT_LEAVE(400, "소유자는 가계부를 탈퇴할 수 없습니다."),

    // Transaction
    TRANSACTION_NOT_FOUND(404, "거래 내역을 찾을 수 없습니다."),

    // Category
    CATEGORY_NOT_FOUND(404, "카테고리를 찾을 수 없습니다.");

    private final int status;
    private final String message;
}
