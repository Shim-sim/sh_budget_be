package com.shbudget.domain.statistics.dto.response;

import java.util.List;

/**
 * 카테고리별 통계 응답 (전체 래퍼)
 */
public record CategoryStatisticsResponse(
        Integer year,
        Integer month,
        Long totalExpense,
        List<CategoryStatistics> categories
) {
}
