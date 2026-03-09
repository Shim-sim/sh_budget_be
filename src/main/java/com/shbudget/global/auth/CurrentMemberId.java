package com.shbudget.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드 파라미터에 사용하여 현재 인증된 회원 ID를 주입합니다.
 * JWT 토큰에서 추출 → 없으면 X-Member-Id 헤더에서 폴백
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentMemberId {
}
