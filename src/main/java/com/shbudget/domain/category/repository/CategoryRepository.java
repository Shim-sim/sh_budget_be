package com.shbudget.domain.category.repository;

import com.shbudget.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 가계부별 카테고리 목록 조회
    List<Category> findAllByBookIdOrderByCreatedAtAsc(Long bookId);

    // 가계부 + 카테고리 ID로 조회
    Optional<Category> findByIdAndBookId(Long id, Long bookId);

    // 가계부 + 카테고리명 중복 체크
    boolean existsByBookIdAndName(Long bookId, String name);

    // 가계부 + 카테고리 존재 여부
    boolean existsByIdAndBookId(Long id, Long bookId);
}
