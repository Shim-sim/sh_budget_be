package com.shbudget.domain.category.service;

import com.shbudget.domain.book.repository.BookMemberRepository;
import com.shbudget.domain.category.dto.request.CategoryCreateRequest;
import com.shbudget.domain.category.dto.request.CategoryUpdateRequest;
import com.shbudget.domain.category.dto.response.CategoryResponse;
import com.shbudget.domain.category.entity.Category;
import com.shbudget.domain.category.repository.CategoryRepository;
import com.shbudget.domain.transaction.repository.TransactionRepository;
import com.shbudget.global.exception.CustomException;
import com.shbudget.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookMemberRepository bookMemberRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public CategoryResponse createCategory(Long memberId, CategoryCreateRequest request) {
        // 가계부 멤버 검증
        validateBookMember(request.bookId(), memberId);

        // 카테고리 이름 중복 체크
        if (categoryRepository.existsByBookIdAndName(request.bookId(), request.name())) {
            throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        // 카테고리 생성
        Category category = Category.create(
                request.bookId(),
                request.name(),
                request.color(),
                request.icon()
        );

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }

    public List<CategoryResponse> getCategoryList(Long memberId, Long bookId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        List<Category> categories = categoryRepository.findAllByBookIdOrderByCreatedAtAsc(bookId);
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long memberId, Long bookId, Long categoryId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Category category = categoryRepository.findByIdAndBookId(categoryId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long memberId, Long bookId, Long categoryId,
                                            CategoryUpdateRequest request) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Category category = categoryRepository.findByIdAndBookId(categoryId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 이름 변경 시 중복 체크
        if (request.name() != null && !request.name().equals(category.getName())) {
            if (categoryRepository.existsByBookIdAndName(bookId, request.name())) {
                throw new CustomException(ErrorCode.DUPLICATE_CATEGORY_NAME);
            }
        }

        // 카테고리 수정
        category.updateCategory(request.name(), request.color(), request.icon());

        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long memberId, Long bookId, Long categoryId) {
        // 가계부 멤버 검증
        validateBookMember(bookId, memberId);

        Category category = categoryRepository.findByIdAndBookId(categoryId, bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 거래 내역 존재 여부 체크
        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new CustomException(ErrorCode.CATEGORY_HAS_TRANSACTIONS);
        }

        categoryRepository.delete(category);
    }

    // === Private Helper Methods ===

    private void validateBookMember(Long bookId, Long memberId) {
        if (!bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)) {
            throw new CustomException(ErrorCode.NOT_BOOK_MEMBER);
        }
    }
}
