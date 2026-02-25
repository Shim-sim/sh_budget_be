package com.shbudget.domain.statistics.dto.response;

/**
 * 멤버별 기여도 통계
 */
public record MemberContributionResponse(
        Long memberId,
        String memberNickname,
        Long totalIncome,       // 해당 멤버의 총 수입
        Long totalExpense,      // 해당 멤버의 총 지출
        Double incomePercentage,   // 전체 수입 대비 비율 (0~100)
        Double expensePercentage   // 전체 지출 대비 비율 (0~100)
) {
}
