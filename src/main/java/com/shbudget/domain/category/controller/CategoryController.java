package com.shbudget.domain.category.controller;

import com.shbudget.domain.category.dto.request.CategoryCreateRequest;
import com.shbudget.domain.category.dto.request.CategoryUpdateRequest;
import com.shbudget.domain.category.dto.response.CategoryResponse;
import com.shbudget.domain.category.service.CategoryService;
import com.shbudget.global.auth.CurrentMemberId;
import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "카테고리 관리 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "409", description = "카테고리 이름 중복")
    })
    public ResponseEntity<ApiResult<CategoryResponse>> createCategory(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(memberId, request);
        return ResponseEntity
                .status(ResponseStatus.CREATED.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.CREATED, response));
    }

    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "가계부의 카테고리 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님")
    })
    public ResponseEntity<ApiResult<List<CategoryResponse>>> getCategoryList(
            @CurrentMemberId Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        List<CategoryResponse> response = categoryService.getCategoryList(memberId, bookId);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "카테고리 상세 조회", description = "카테고리 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<CategoryResponse>> getCategory(
            @CurrentMemberId Long memberId,
            @Parameter(description = "카테고리 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        CategoryResponse response = categoryService.getCategoryById(memberId, bookId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "카테고리 수정", description = "카테고리 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "카테고리 이름 중복")
    })
    public ResponseEntity<ApiResult<CategoryResponse>> updateCategory(
            @CurrentMemberId Long memberId,
            @Parameter(description = "카테고리 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryService.updateCategory(memberId, bookId, id, request);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. (거래 내역이 없는 경우만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "거래 내역이 있는 카테고리"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> deleteCategory(
            @CurrentMemberId Long memberId,
            @Parameter(description = "카테고리 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        categoryService.deleteCategory(memberId, bookId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, null));
    }
}
