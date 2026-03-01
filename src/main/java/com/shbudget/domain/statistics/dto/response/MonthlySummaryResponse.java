package com.shbudget.domain.statistics.dto.response;

/**
 * 월별 수입/지출 요약
 */
public record MonthlySummaryResponse(
        Integer year,
        Integer month,
        Long totalIncome,      // 총 수입
        Long totalExpense,     // 총 지출
        Long netIncome,        // 순수익 (수입 - 지출)
        Long totalAssets       // 총 자산 (현재 시점)
) {
    public static MonthlySummaryResponse of(Integer year, Integer month, Long totalIncome,
                                             Long totalExpense, Long totalAssets) {
        return new MonthlySummaryResponse(
                year,
                month,
                totalIncome,
                totalExpense,
                totalIncome - totalExpense,
                totalAssets
        );
    }
}
