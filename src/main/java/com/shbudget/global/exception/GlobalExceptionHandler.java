package com.shbudget.global.exception;

import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResult.error(errorCode.getStatus(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? ResponseStatus.BAD_REQUEST.getMessage()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(ResponseStatus.BAD_REQUEST.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.BAD_REQUEST, message, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity
                .status(ResponseStatus.BAD_REQUEST.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(ResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.INTERNAL_SERVER_ERROR));
    }
}
