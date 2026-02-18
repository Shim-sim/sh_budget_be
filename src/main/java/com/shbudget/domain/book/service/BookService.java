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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookMemberRepository bookMemberRepository;

    @Transactional
    public BookResponse createBookForMember(Long memberId, String bookName) {
        // Create book
        Book book = Book.create(bookName, memberId);
        Book savedBook = bookRepository.save(book);

        // Create owner membership
        BookMember bookMember = BookMember.createOwner(savedBook.getId(), memberId);
        bookMemberRepository.save(bookMember);

        return BookResponse.from(savedBook);
    }

    public BookResponse getMyBook(Long memberId) {
        BookMember bookMember = bookMemberRepository.findByMemberIdAndRole(memberId, BookMemberRole.OWNER)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        Book book = bookRepository.findById(bookMember.getBookId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse updateBook(Long bookId, Long memberId, BookUpdateRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        validateBookOwner(bookId, memberId);

        book.updateName(request.name());
        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse regenerateInviteCode(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        validateBookOwner(bookId, memberId);

        // Regenerate until unique code is found
        String newCode;
        do {
            book.regenerateInviteCode();
            newCode = book.getInviteCode();
        } while (bookRepository.existsByInviteCode(newCode) && !newCode.equals(book.getInviteCode()));

        return BookResponse.from(book);
    }

    @Transactional
    public void deleteBook(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        validateBookOwner(bookId, memberId);

        bookMemberRepository.deleteAllByBookId(bookId);
        bookRepository.delete(book);
    }

    private void validateBookOwner(Long bookId, Long memberId) {
        BookMember bookMember = bookMemberRepository.findByBookIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_BOOK_MEMBER));

        if (!bookMember.isOwner()) {
            throw new CustomException(ErrorCode.NOT_BOOK_OWNER);
        }
    }
}
