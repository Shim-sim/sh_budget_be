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

    // Auth
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),

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

    // Asset
    ASSET_NOT_FOUND(404, "자산을 찾을 수 없습니다."),
    ASSET_HAS_TRANSACTIONS(400, "거래 내역이 있는 자산은 삭제할 수 없습니다."),

    // Transaction
    TRANSACTION_NOT_FOUND(404, "거래 내역을 찾을 수 없습니다."),
    INSUFFICIENT_BALANCE(400, "잔액이 부족합니다."),
    SAME_ASSET_TRANSFER(400, "같은 자산으로 이체할 수 없습니다."),
    INVALID_TRANSACTION_TYPE(400, "유효하지 않은 거래 타입입니다."),
    INVALID_AMOUNT(400, "금액은 0보다 커야 합니다."),
    FUTURE_DATE_NOT_ALLOWED(400, "미래 날짜는 입력할 수 없습니다."),
    ASSET_REQUIRED_FOR_INCOME_EXPENSE(400, "수입/지출은 자산 ID가 필수입니다."),
    ASSETS_REQUIRED_FOR_TRANSFER(400, "이체는 출발/도착 자산 ID가 필수입니다."),

    // Recurring
    RECURRING_NOT_FOUND(404, "반복 거래를 찾을 수 없습니다."),

    // Category
    CATEGORY_NOT_FOUND(404, "카테고리를 찾을 수 없습니다."),
    DUPLICATE_CATEGORY_NAME(409, "이미 존재하는 카테고리 이름입니다."),
    CATEGORY_HAS_TRANSACTIONS(400, "거래 내역이 있는 카테고리는 삭제할 수 없습니다.");

    private final int status;
    private final String message;
}
