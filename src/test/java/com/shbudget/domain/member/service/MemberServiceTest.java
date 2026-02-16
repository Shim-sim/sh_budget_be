package com.shbudget.domain.member.service;

import com.shbudget.domain.member.dto.request.MemberCreateRequest;
import com.shbudget.domain.member.dto.request.MemberUpdateRequest;
import com.shbudget.domain.member.dto.response.MemberResponse;
import com.shbudget.domain.member.entity.Member;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입 성공")
    void createMember_Success() {
        // given
        MemberCreateRequest request = new MemberCreateRequest(
                "test@example.com",
                "테스터"
        );
        Member member = Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .build();

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        MemberResponse response = memberService.createMember(request);

        // then
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("테스터");
        verify(memberRepository).existsByEmail("test@example.com");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복으로 회원 가입 실패")
    void createMember_DuplicateEmail() {
        // given
        MemberCreateRequest request = new MemberCreateRequest(
                "test@example.com",
                "테스터"
        );
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        verify(memberRepository).existsByEmail("test@example.com");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 ID로 조회 성공")
    void findById_Success() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .email("test@example.com")
                .nickname("테스터")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.findById(memberId);

        // then
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("테스터");
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외 발생")
    void findById_NotFound() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findById(memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMember_Success() {
        // given
        Long memberId = 1L;
        MemberUpdateRequest request = new MemberUpdateRequest(
                "수정된닉네임",
                "https://example.com/profile.jpg"
        );
        Member member = Member.builder()
                .email("test@example.com")
                .nickname("테스터")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.updateMember(memberId, request);

        // then
        assertThat(response.nickname()).isEqualTo("수정된닉네임");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 수정 시 예외 발생")
    void updateMember_NotFound() {
        // given
        Long memberId = 999L;
        MemberUpdateRequest request = new MemberUpdateRequest(
                "수정된닉네임",
                "https://example.com/profile.jpg"
        );
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateMember(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("닉네임만 수정")
    void updateMember_OnlyNickname() {
        // given
        Long memberId = 1L;
        MemberUpdateRequest request = new MemberUpdateRequest(
                "수정된닉네임",
                null
        );
        Member member = Member.builder()
                .email("test@example.com")
                .nickname("테스터")
                .profileImageUrl("https://example.com/old.jpg")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.updateMember(memberId, request);

        // then
        assertThat(response.nickname()).isEqualTo("수정된닉네임");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/old.jpg");
    }
}
