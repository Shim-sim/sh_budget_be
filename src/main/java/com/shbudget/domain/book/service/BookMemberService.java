package com.shbudget.domain.book.service;

import com.shbudget.domain.book.dto.BookJoinRequest;
import com.shbudget.domain.book.dto.BookMemberResponse;
import com.shbudget.domain.book.entity.Book;
import com.shbudget.domain.book.entity.BookMember;
import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.book.repository.BookRepository;
import com.shbudget.domain.member.repository.MemberRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookMemberService {

    private final BookRepository bookRepository;
    private final BookMemberRepository bookMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public BookMemberResponse joinBook(Long memberId, BookJoinRequest request) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Book book = bookRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INVITE_CODE));

        // Check if already joined
        if (bookMemberRepository.existsByBookIdAndMemberId(book.getId(), memberId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_BOOK);
        }

        // Create member
        BookMember bookMember = BookMember.createMember(book.getId(), memberId);
        BookMember savedBookMember = bookMemberRepository.save(bookMember);

        return BookMemberResponse.from(savedBookMember);
    }

    public List<BookMemberResponse> getBookMembers(Long bookId, Long memberId) {
        // Validate requester is a member of the book
        validateBookMember(bookId, memberId);

        List<BookMember> bookMembers = bookMemberRepository.findAllByBookId(bookId);
        return bookMembers.stream()
                .map(BookMemberResponse::from)
                .toList();
    }

    @Transactional
    public void leaveOrRemoveMember(Long bookId, Long requesterId, Long targetMemberId) {
        // Validate book exists
        bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        BookMember requester = bookMemberRepository.findByBookIdAndMemberId(bookId, requesterId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOOK_MEMBER));

        BookMember target = bookMemberRepository.findByBookIdAndMemberId(bookId, targetMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOOK_MEMBER));

        // Case 1: Self-leave
        if (requesterId.equals(targetMemberId)) {
            if (target.isOwner()) {
                throw new CustomException(ErrorCode.OWNER_CANNOT_LEAVE);
            }
            bookMemberRepository.delete(target);
            return;
        }

        // Case 2: Owner removing another member
        if (!requester.isOwner()) {
            throw new CustomException(ErrorCode.NOT_BOOK_OWNER);
        }

        if (target.isOwner()) {
            throw new CustomException(ErrorCode.OWNER_CANNOT_LEAVE);
        }

        bookMemberRepository.delete(target);
    }

    private void validateBookMember(Long bookId, Long memberId) {
        if (!bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new CustomException(ErrorCode.NOT_BOOK_MEMBER);
        }
    }
}
