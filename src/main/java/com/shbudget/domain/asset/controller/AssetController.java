package com.shbudget.domain.asset.controller;

import com.shbudget.domain.asset.dto.AssetCreateRequest;
import com.shbudget.domain.asset.dto.AssetResponse;
import com.shbudget.domain.asset.dto.AssetSummaryResponse;
import com.shbudget.domain.asset.dto.AssetUpdateRequest;
import com.shbudget.domain.asset.service.AssetService;
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

@Tag(name = "Asset", description = "자산 관리 API")
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @Operation(summary = "자산 생성", description = "가계부에 새로운(첫 등록) 자산을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "자산 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<AssetResponse>> createAsset(
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Valid @RequestBody AssetCreateRequest request
    ) {
        AssetResponse response = assetService.createAsset(memberId, bookId, request);
        return ResponseEntity
                .status(ResponseStatus.CREATED.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.CREATED, response));
    }

    @GetMapping
    @Operation(summary = "자산 목록 조회", description = "가계부의 모든 자산 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<List<AssetResponse>>> getAssets(
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        List<AssetResponse> response = assetService.getAssets(memberId, bookId);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "자산 상세 조회", description = "특정 자산의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<AssetResponse>> getAsset(
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "자산 ID", required = true)
            @PathVariable Long id
    ) {
        AssetResponse response = assetService.getAsset(memberId, bookId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "자산 수정", description = "자산 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<AssetResponse>> updateAsset(
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "자산 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest request
    ) {
        AssetResponse response = assetService.updateAsset(memberId, bookId, id, request);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자산 삭제", description = "자산을 삭제합니다. 거래 내역이 있는 자산은 삭제할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "거래 내역이 있는 자산"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "자산을 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<Void>> deleteAsset(
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "자산 ID", required = true)
            @PathVariable Long id
    ) {
        assetService.deleteAsset(memberId, bookId, id);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, null));
    }

    @GetMapping("/total")
    @Operation(summary = "총 자산 조회", description = "가계부의 모든 자산 합계를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<ApiResult<AssetSummaryResponse>> getTotalAssets(
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId
    ) {
        AssetSummaryResponse response = assetService.getTotalAssets(memberId, bookId);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }
}
