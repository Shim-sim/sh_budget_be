package com.shbudget.domain.auth.service;

import com.shbudget.domain.auth.dto.request.LoginRequest;
import com.shbudget.domain.auth.dto.request.RefreshRequest;
import com.shbudget.domain.auth.dto.request.RegisterRequest;
import com.shbudget.domain.auth.dto.response.AuthResponse;
import com.shbudget.domain.book.service.BookService;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.global.config.jwt.JwtProvider;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final BookService bookService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = Member.createWithPassword(request.email(), encodedPassword, request.nickname());
        Member savedMember = memberRepository.save(member);

        // 새 회원에게 기본 가계부 생성
        String bookName = savedMember.getNickname() + "의 가계부";
        bookService.createBookForMember(savedMember.getId(), bookName);

        return createAuthResponse(savedMember);
    }

    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 기존 회원 중 비밀번호 미설정 회원은 비밀번호 없이 로그인 허용 (마이그레이션 기간)
        if (member.getPassword() != null) {
            if (!passwordEncoder.matches(request.password(), member.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_PASSWORD);
            }
        }

        return createAuthResponse(member);
    }

    public AuthResponse refresh(RefreshRequest request) {
        if (!jwtProvider.validateToken(request.refreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtProvider.getMemberId(request.refreshToken());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return createAuthResponse(member);
    }

    private AuthResponse createAuthResponse(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getId())
                .build();
    }
}
