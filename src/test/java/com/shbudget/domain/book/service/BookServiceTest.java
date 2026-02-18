package com.shbudget.domain.book.service;

import com.shbudget.domain.book.dto.BookResponse;
import com.shbudget.domain.book.dto.BookUpdateRequest;
import com.shbudget.domain.book.entity.Book;
import com.shbudget.domain.book.entity.BookMember;
import com.shbudget.domain.book.entity.BookMemberRole;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.book.repository.BookRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMemberRepository bookMemberRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("회원용 가계부 생성 성공")
    void createBookForMember() {
        // given
        Long memberId = 1L;
        String bookName = "테스트 가계부";
        Book book = Book.create(bookName, memberId);
        given(bookRepository.save(any(Book.class))).willReturn(book);
        given(bookMemberRepository.save(any(BookMember.class))).willReturn(BookMember.createOwner(1L, memberId));

        // when
        BookResponse response = bookService.createBookForMember(memberId, bookName);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(bookName);
        assertThat(response.ownerId()).isEqualTo(memberId);
        verify(bookRepository).save(any(Book.class));
        verify(bookMemberRepository).save(any(BookMember.class));
    }

    @Test
    @DisplayName("내 가계부 조회 성공")
    void getMyBook() {
        // given
        Long memberId = 1L;
        Book book = Book.create("테스트 가계부", memberId);
        BookMember bookMember = BookMember.createOwner(1L, memberId);
        given(bookMemberRepository.findByMemberIdAndRole(memberId, BookMemberRole.OWNER))
                .willReturn(Optional.of(bookMember));
        given(bookRepository.findById(1L)).willReturn(Optional.of(book));

        // when
        BookResponse response = bookService.getMyBook(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.ownerId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("내 가계부 조회 실패 - 가계부 없음")
    void getMyBook_notFound() {
        // given
        Long memberId = 1L;
        given(bookMemberRepository.findByMemberIdAndRole(memberId, BookMemberRole.OWNER))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.getMyBook(memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("가계부 이름 수정 성공")
    void updateBook() {
        // given
        Long bookId = 1L;
        Long memberId = 1L;
        String newName = "새 가계부 이름";
        BookUpdateRequest request = new BookUpdateRequest(newName);

        Book book = Book.create("기존 이름", memberId);
        BookMember bookMember = BookMember.createOwner(bookId, memberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, memberId))
                .willReturn(Optional.of(bookMember));

        // when
        BookResponse response = bookService.updateBook(bookId, memberId, request);

        // then
        assertThat(response.name()).isEqualTo(newName);
    }

    @Test
    @DisplayName("가계부 이름 수정 실패 - 소유자가 아님")
    void updateBook_notOwner() {
        // given
        Long bookId = 1L;
        Long memberId = 2L;
        BookUpdateRequest request = new BookUpdateRequest("새 이름");

        Book book = Book.create("기존 이름", 1L);
        BookMember bookMember = BookMember.createMember(bookId, memberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, memberId))
                .willReturn(Optional.of(bookMember));

        // when & then
        assertThatThrownBy(() -> bookService.updateBook(bookId, memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }

    @Test
    @DisplayName("초대 코드 재생성 성공")
    void regenerateInviteCode() {
        // given
        Long bookId = 1L;
        Long memberId = 1L;
        Book book = Book.create("테스트 가계부", memberId);
        String originalCode = book.getInviteCode();
        BookMember bookMember = BookMember.createOwner(bookId, memberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, memberId))
                .willReturn(Optional.of(bookMember));
        given(bookRepository.existsByInviteCode(anyString())).willReturn(false);

        // when
        BookResponse response = bookService.regenerateInviteCode(bookId, memberId);

        // then
        assertThat(response.inviteCode()).isNotEqualTo(originalCode);
        assertThat(response.inviteCode()).hasSize(6);
    }

    @Test
    @DisplayName("가계부 삭제 성공")
    void deleteBook() {
        // given
        Long bookId = 1L;
        Long memberId = 1L;
        Book book = Book.create("테스트 가계부", memberId);
        BookMember bookMember = BookMember.createOwner(bookId, memberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, memberId))
                .willReturn(Optional.of(bookMember));

        // when
        bookService.deleteBook(bookId, memberId);

        // then
        verify(bookMemberRepository).deleteAllByBookId(bookId);
        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("가계부 삭제 실패 - 소유자가 아님")
    void deleteBook_notOwner() {
        // given
        Long bookId = 1L;
        Long memberId = 2L;
        Book book = Book.create("테스트 가계부", 1L);
        BookMember bookMember = BookMember.createMember(bookId, memberId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMemberRepository.findByBookIdAndMemberId(bookId, memberId))
                .willReturn(Optional.of(bookMember));

        // when & then
        assertThatThrownBy(() -> bookService.deleteBook(bookId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_OWNER);
    }
}
