package com.shbudget.domain.statistics.dto.response;

/**
 * 카테고리별 통계
 */
public record CategoryStatistics(
        Long categoryId,
        String categoryName,
        Long totalAmount,      // 해당 카테고리 총 금액
        Integer transactionCount,  // 거래 건수
        Double percentage      // 전체 지출 대비 비율 (0~100)
) {
}
