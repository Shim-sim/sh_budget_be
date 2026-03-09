package com.shbudget.domain.auth.controller;

import com.shbudget.domain.auth.dto.request.LoginRequest;
import com.shbudget.domain.auth.dto.request.RefreshRequest;
import com.shbudget.domain.auth.dto.request.RegisterRequest;
import com.shbudget.domain.auth.dto.response.AuthResponse;
import com.shbudget.domain.auth.service.AuthService;
import com.shbudget.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원가입하고 JWT 토큰을 발급합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResult<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Operation(summary = "로그인", description = "이메일, 비밀번호로 로그인하고 JWT 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
