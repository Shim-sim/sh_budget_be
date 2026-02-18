package com.shbudget.domain.book.repository;

import com.shbudget.domain.book.entity.BookMember;
import com.shbudget.domain.book.entity.BookMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMemberRepository extends JpaRepository<BookMember, Long> {

    Optional<BookMember> findByBookIdAndMemberId(Long bookId, Long memberId);

    List<BookMember> findAllByBookId(Long bookId);

    List<BookMember> findAllByMemberId(Long memberId);

    Optional<BookMember> findByMemberIdAndRole(Long memberId, BookMemberRole role);

    boolean existsByBookIdAndMemberId(Long bookId, Long memberId);

    void deleteAllByBookId(Long bookId);
}
