package com.shbudget.domain.statistics.controller;

import com.shbudget.domain.statistics.dto.response.CategoryStatisticsResponse;
import com.shbudget.domain.statistics.dto.response.MemberContributionResponse;
import com.shbudget.domain.statistics.dto.response.MonthlySummaryResponse;
import com.shbudget.domain.statistics.service.StatisticsService;
import com.shbudget.global.auth.CurrentMemberId;
import com.shbudget.global.common.ApiResult;
import com.shbudget.global.common.ResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Statistics", description = "통계 API")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly-summary")
    @Operation(summary = "월별 수입/지출 요약", description = "특정 월의 총 수입, 총 지출, 순수익, 총 자산을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님")
    })
    public ResponseEntity<ApiResult<MonthlySummaryResponse>> getMonthlySummary(
            @CurrentMemberId Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "년도", example = "2024", required = true)
            @RequestParam Integer year,
            @Parameter(description = "월", example = "5", required = true)
            @RequestParam Integer month
    ) {
        MonthlySummaryResponse response = statisticsService.getMonthlySummary(memberId, bookId, year, month);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @GetMapping("/category")
    @Operation(summary = "카테고리별 지출 통계", description = "특정 월의 카테고리별 지출 금액과 비율을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님")
    })
    public ResponseEntity<ApiResult<CategoryStatisticsResponse>> getCategoryStatistics(
            @CurrentMemberId Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "년도", example = "2024", required = true)
            @RequestParam Integer year,
            @Parameter(description = "월", example = "5", required = true)
            @RequestParam Integer month
    ) {
        CategoryStatisticsResponse response = statisticsService.getCategoryStatistics(memberId, bookId, year, month);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }

    @GetMapping("/member-contribution")
    @Operation(summary = "멤버별 기여도 통계", description = "특정 월의 멤버별 수입/지출 기여도를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "가계부 멤버가 아님")
    })
    public ResponseEntity<ApiResult<List<MemberContributionResponse>>> getMemberContribution(
            @CurrentMemberId Long memberId,
            @Parameter(description = "가계부 ID", required = true)
            @RequestParam Long bookId,
            @Parameter(description = "년도", example = "2024", required = true)
            @RequestParam Integer year,
            @Parameter(description = "월", example = "5", required = true)
            @RequestParam Integer month
    ) {
        List<MemberContributionResponse> response = statisticsService.getMemberContribution(memberId, bookId, year, month);
        return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
    }
}
