package com.shbudget.global.auth;

import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentMemberIdResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMemberId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        // 1순위: JWT에서 추출한 memberId (SecurityContext)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long memberId) {
            return memberId;
        }

        // 2순위: X-Member-Id 헤더 폴백 (JWT 전환 완료 전 호환)
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            String headerValue = request.getHeader("X-Member-Id");
            if (headerValue != null && !headerValue.isBlank()) {
                try {
                    return Long.valueOf(headerValue);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}
