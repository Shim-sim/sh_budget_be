package com.shbudget.domain.book.repository;

import com.shbudget.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    Optional<Book> findByOwnerId(Long ownerId);
}
