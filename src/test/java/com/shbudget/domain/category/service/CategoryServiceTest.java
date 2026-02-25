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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookMemberRepository bookMemberRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        CategoryCreateRequest request = new CategoryCreateRequest(
                bookId,
                "식비",
                "#FF5733",
                "food"
        );

        Category category = Category.create(bookId, "식비", "#FF5733", "food");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.existsByBookIdAndName(bookId, "식비")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // when
        CategoryResponse response = categoryService.createCategory(memberId, request);

        // then
        assertThat(response.name()).isEqualTo("식비");
        assertThat(response.color()).isEqualTo("#FF5733");
        assertThat(response.icon()).isEqualTo("food");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 이름 중복으로 생성 실패")
    void createCategory_DuplicateName() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        CategoryCreateRequest request = new CategoryCreateRequest(
                bookId,
                "식비",
                "#FF5733",
                "food"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.existsByBookIdAndName(bookId, "식비")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_CATEGORY_NAME);
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategoryList() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;

        Category category1 = Category.create(bookId, "식비", "#FF5733", "food");
        Category category2 = Category.create(bookId, "교통", "#3498DB", "transport");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findAllByBookIdOrderByCreatedAtAsc(bookId))
                .thenReturn(List.of(category1, category2));

        // when
        List<CategoryResponse> response = categoryService.getCategoryList(memberId, bookId);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("식비");
        assertThat(response.get(1).name()).isEqualTo("교통");
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getCategoryById() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long categoryId = 1L;

        Category category = Category.create(bookId, "식비", "#FF5733", "food");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findByIdAndBookId(categoryId, bookId))
                .thenReturn(Optional.of(category));

        // when
        CategoryResponse response = categoryService.getCategoryById(memberId, bookId, categoryId);

        // then
        assertThat(response.name()).isEqualTo("식비");
        assertThat(response.color()).isEqualTo("#FF5733");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long categoryId = 1L;

        Category category = Category.create(bookId, "식비", "#FF5733", "food");
        CategoryUpdateRequest request = new CategoryUpdateRequest(
                "외식비",
                "#E74C3C",
                "restaurant"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findByIdAndBookId(categoryId, bookId))
                .thenReturn(Optional.of(category));
        when(categoryRepository.existsByBookIdAndName(bookId, "외식비")).thenReturn(false);

        // when
        CategoryResponse response = categoryService.updateCategory(memberId, bookId, categoryId, request);

        // then
        assertThat(response.name()).isEqualTo("외식비");
        assertThat(response.color()).isEqualTo("#E74C3C");
        assertThat(response.icon()).isEqualTo("restaurant");
    }

    @Test
    @DisplayName("카테고리 수정 시 이름 중복으로 실패")
    void updateCategory_DuplicateName() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long categoryId = 1L;

        Category category = Category.create(bookId, "식비", "#FF5733", "food");
        CategoryUpdateRequest request = new CategoryUpdateRequest(
                "교통비",  // 이미 존재하는 이름
                "#E74C3C",
                "transport"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findByIdAndBookId(categoryId, bookId))
                .thenReturn(Optional.of(category));
        when(categoryRepository.existsByBookIdAndName(bookId, "교통비")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(memberId, bookId, categoryId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_CATEGORY_NAME);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long categoryId = 1L;

        Category category = Category.create(bookId, "식비", "#FF5733", "food");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findByIdAndBookId(categoryId, bookId))
                .thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategoryId(categoryId)).thenReturn(false);

        // when
        categoryService.deleteCategory(memberId, bookId, categoryId);

        // then
        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("거래 내역이 있는 카테고리 삭제 시도 시 실패")
    void deleteCategory_HasTransactions() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long categoryId = 1L;

        Category category = Category.create(bookId, "식비", "#FF5733", "food");

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findByIdAndBookId(categoryId, bookId))
                .thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategoryId(categoryId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(memberId, bookId, categoryId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_HAS_TRANSACTIONS);
    }

    @Test
    @DisplayName("가계부 멤버가 아닌 경우 카테고리 생성 실패")
    void createCategory_NotBookMember() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        CategoryCreateRequest request = new CategoryCreateRequest(
                bookId,
                "식비",
                "#FF5733",
                "food"
        );

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(memberId, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_BOOK_MEMBER);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회 시 실패")
    void getCategoryById_NotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 1L;
        Long categoryId = 999L;

        when(bookMemberRepository.existsByBookIdAndMemberId(bookId, memberId)).thenReturn(true);
        when(categoryRepository.findByIdAndBookId(categoryId, bookId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(memberId, bookId, categoryId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }
}
