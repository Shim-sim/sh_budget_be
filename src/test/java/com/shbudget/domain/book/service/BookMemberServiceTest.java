package com.shbudget.domain.book.service;

import com.shbudget.domain.book.dto.BookJoinRequest;
import com.shbudget.domain.book.dto.BookMemberResponse;
import com.shbudget.domain.book.entity.Book;
import com.shbudget.domain.book.entity.BookMember;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.book.repository.BookRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookMemberServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMemberRepository bookMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BookMemberService bookMemberService;

    @Test
    @DisplayName("가계부 참여 성공")
    void joinBook() {
        // given
        Long memberId = 2L;
        String inviteCode = "ABC123";
        BookJoinRequest request = new BookJoinRequest(inviteCode);

        Member member = Member.create("test@example.com", "테스터");
        Book book = Book.create("테스트 가계부", 1L);
        BookMember bookMember = BookMember.createMember(1L, memberId);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(bookRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(book));
        given(bookMemberRepository.existsByBookIdAndMemberId(any(), any())).willReturn(false);
        given(bookMemberRepository.save(any(BookMember.class))).willReturn(bookMember);

        // when
        BookMemberResponse response = bookMemberService.joinBook(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(memberId);
        verify(bookMemberRepository).save(any(BookMember.class));
    }

    @Test
    @DisplayName("가계부 참여 실패 - 회원 없음")
    void joinBook_memberNotFound() {
        // given
        Long memberId = 2L;
        BookJoinRequest request = new BookJoinRequest("ABC123");
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookMemberService.joinBook(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("가계부 참여 실패 - 유효하지 않은 초대 코드")
    void joinBook_invalidInviteCode() {
        // given
        Long memberId = 2L;
        String inviteCode = "INVALID";
        BookJoinRequest request = new BookJoinRequest(inviteCode);

        Member member = Member.create("test@example.com", "테스터");
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(bookRepository.findByInviteCode(inviteCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookMemberService.joinBook(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    @DisplayName("가계부 참여 실패 - 이미 참여 중")
    void joinBook_alreadyJoined() {
        // given
        Long memberId = 2L;
        String inviteCode = "ABC123";
        BookJoinRequest request = new BookJoinRequest(inviteCode);

        Member member = Member.create("test@example.com", "테스터");
        Book book = Book.create("테스트 가계부", 1L);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(bookRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(book));
        given(bookMemberRepository.existsByBookIdAndMemberId(any(), any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> bookMemberService.joinBook(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_JOINED_BOOK);
    }

    @Test
    @DisplayName("가계부 멤버 목록 조회 성공")
    void getBookMembers() {
        // given
        Long bookId = 1L;
        Long memberId = 1L;

        BookMember owner = BookMember.createOwner(bookId, memberId);
        BookMember member = BookMember.createMember(bookId, 2L);

        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(true);
        given(bookMemberRepository.findAllByBookId(bookId)).willReturn(List.of(owner, member));

        // when
        List<BookMemberResponse> responses = bookMemberService.getBookMembers(bookId, memberId);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("가계부 멤버 목록 조회 실패 - 멤버가 아님")
    void getBookMembers_notMember() {
        // given
        Long bookId = 1L;
        Long memberId = 999L;
        given(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> bookMemberService.getBookMembers(bookId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_MEMBER);
    }

    @Test
    @DisplayName("가계부 탈퇴 성공 - 일반 멤버")
    void leaveMember() {
        // given
        Long bookId = 1L;
        Long memberId = 2L;
        Book book = Book.create("테스트 가계부", 1L);
        BookMember member = BookMember.createMember(bookId, memberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, memberId))
                .willReturn(Optional.of(member));

        // when
        bookMemberService.leaveOrRemoveMember(bookId, memberId, memberId);

        // then
        verify(bookMemberRepository).delete(member);
    }

    @Test
    @DisplayName("가계부 탈퇴 실패 - 소유자는 탈퇴 불가")
    void leaveMember_ownerCannotLeave() {
        // given
        Long bookId = 1L;
        Long ownerId = 1L;
        Book book = Book.create("테스트 가계부", ownerId);
        BookMember owner = BookMember.createOwner(bookId, ownerId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, ownerId))
                .willReturn(Optional.of(owner));

        // when & then
        assertThatThrownBy(() -> bookMemberService.leaveOrRemoveMember(bookId, ownerId, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OWNER_CANNOT_LEAVE);
    }

    @Test
    @DisplayName("멤버 제거 성공 - 소유자가 일반 멤버 제거")
    void removeMember() {
        // given
        Long bookId = 1L;
        Long ownerId = 1L;
        Long targetMemberId = 2L;

        Book book = Book.create("테스트 가계부", ownerId);
        BookMember owner = BookMember.createOwner(bookId, ownerId);
        BookMember target = BookMember.createMember(bookId, targetMemberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, ownerId))
                .willReturn(Optional.of(owner));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, targetMemberId))
                .willReturn(Optional.of(target));

        // when
        bookMemberService.leaveOrRemoveMember(bookId, ownerId, targetMemberId);

        // then
        verify(bookMemberRepository).delete(target);
    }

    @Test
    @DisplayName("멤버 제거 실패 - 소유자가 아닌 사람이 시도")
    void removeMember_notOwner() {
        // given
        Long bookId = 1L;
        Long requesterId = 2L;
        Long targetMemberId = 3L;

        Book book = Book.create("테스트 가계부", 1L);
        BookMember requester = BookMember.createMember(bookId, requesterId);
        BookMember target = BookMember.createMember(bookId, targetMemberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, requesterId))
                .willReturn(Optional.of(requester));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, targetMemberId))
                .willReturn(Optional.of(target));

        // when & then
        assertThatThrownBy(() -> bookMemberService.leaveOrRemoveMember(bookId, requesterId, targetMemberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }
}
