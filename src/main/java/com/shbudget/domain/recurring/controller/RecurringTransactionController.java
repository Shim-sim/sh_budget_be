package com.shbudget.domain.recurring.controller;

import com.shbudget.domain.recurring.dto.request.RecurringCreateRequest;
import com.shbudget.domain.recurring.dto.response.RecurringResponse;
import com.shbudget.domain.recurring.service.RecurringTransactionService;
import com.shbudget.global.auth.CurrentMemberId;
import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recurring", description = "반복 거래 API")
@RestController
@RequestMapping("/api/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    @PostMapping
    @Operation(summary = "반복 거래 등록", description = "매월 반복 거래를 등록합니다.")
    public ResponseEntity<ApiResult<RecurringResponse>> create(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody RecurringCreateRequest request
    ) {
        RecurringResponse response = recurringService.create(memberId, request);
        return ResponseEntity
                .status(ResponseStatus.CREATED.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.CREATED, response));
    }

    @GetMapping
    @Operation(summary = "반복 거래 목록 조회", description = "가계부의 활성 반복 거래 목록을 조회합니다.")
    public ResponseEntity<ApiResult<List<RecurringResponse>>> getList(
            @CurrentMemberId Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        List<RecurringResponse> response = recurringService.getList(memberId, bookId);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "반복 거래 삭제", description = "반복 거래를 비활성화합니다.")
    public ResponseEntity<ApiResult<Void>> delete(
            @CurrentMemberId Long memberId,
            @PathVariable Long id
    ) {
        recurringService.delete(memberId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, null));
    }
}
