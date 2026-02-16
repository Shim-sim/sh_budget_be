package com.shbudget.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.web.util.HtmlUtils;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private final int status;
    private final String message;
    private final T data;

    private ApiResult(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private ApiResult(ResponseStatus responseStatus, T data) {
        this.status = responseStatus.getStatusCode();
        this.message = responseStatus.getMessage();
        this.data = data;
    }

    public static <T> ApiResult<T> of(ResponseStatus responseStatus) {
        return new ApiResult<>(responseStatus, null);
    }

    public static <T> ApiResult<T> of(ResponseStatus responseStatus, T data) {
        return new ApiResult<>(responseStatus, data);
    }

    public static <T> ApiResult<T> of(ResponseStatus responseStatus, String customMessage, T data) {
        String safeMessage = customMessage != null ? HtmlUtils.htmlEscape(customMessage) : responseStatus.getMessage();
        return new ApiResult<>(responseStatus.getStatusCode(), safeMessage, data);
    }

    public static <T> ApiResult<T> success(T data) {
        return of(ResponseStatus.SUCCESS, data);
    }

    public static <T> ApiResult<T> created(T data) {
        return of(ResponseStatus.CREATED, data);
    }

    public static <T> ApiResult<T> error(int status, String message) {
        String safeMessage = message != null ? HtmlUtils.htmlEscape(message) : "오류가 발생했습니다.";
        return new ApiResult<>(status, safeMessage, null);
    }

}
