package com.shbudget.domain.transaction.controller;

import com.shbudget.domain.transaction.dto.request.TransactionCreateRequest;
import com.shbudget.domain.transaction.dto.request.TransactionUpdateRequest;
import com.shbudget.domain.transaction.dto.response.TransactionResponse;
import com.shbudget.domain.transaction.entity.TransactionType;
import com.shbudget.domain.transaction.service.TransactionService;
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

@Tag(name = "Transaction", description = "거래 관리 API")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "거래 생성", description = "새로운 거래를 생성합니다. (INCOME, EXPENSE, TRANSFER)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "거래 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 또는 잔액 부족"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<TransactionResponse>> createTransaction(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody TransactionCreateRequest request
    ) {
        TransactionResponse response = transactionService.createTransaction(memberId, request);
        return ResponseEntity
                .status(ResponseStatus.CREATED.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.CREATED, response));
    }

    @GetMapping
    @Operation(summary = "거래 목록 조회", description = "가계부의 거래 내역을 조회합니다. (월별, 타입별 필터 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님")
    })
    public ResponseEntity<ApiResult<List<TransactionResponse>>> getTransactionList(
            @CurrentMemberId Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "월별 필터 (YYYY-MM)", example = "2024-05")
            @RequestParam(required = false) String month,
            @Parameter(description = "거래 타입 필터 (INCOME, EXPENSE, TRANSFER)")
            @RequestParam(required = false) TransactionType type
    ) {
        List<TransactionResponse> response = transactionService.getTransactionList(memberId, bookId, month, type);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "거래 상세 조회", description = "거래 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "거래를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<TransactionResponse>> getTransaction(
            @CurrentMemberId Long memberId,
            @Parameter(description = "거래 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        TransactionResponse response = transactionService.getTransactionById(memberId, bookId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "거래 수정", description = "거래 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력 또는 잔액 부족"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "거래를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<TransactionResponse>> updateTransaction(
            @CurrentMemberId Long memberId,
            @Parameter(description = "거래 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        TransactionResponse response = transactionService.updateTransaction(memberId, bookId, id, request);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "거래 삭제", description = "거래를 삭제하고 자산 잔액을 복구합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "거래를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> deleteTransaction(
            @CurrentMemberId Long memberId,
            @Parameter(description = "거래 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        transactionService.deleteTransaction(memberId, bookId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, null));
    }
}
