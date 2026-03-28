package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa phân tích xu hướng điểm thưởng theo thời gian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing reward points trend analysis over time")
public class PointsTrendAnalyticsResponse {

    // === THÔNG TIN THỜI GIAN ===
    @Schema(description = "Analysis start time", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Analysis end time", example = "2025-01-31T23:59:59")
    private LocalDateTime endDate;

    // === THỐNG KÊ TRONG KHOẢNG THỜI GIAN ===
    @Schema(description = "Total points earned in the period", example = "50000")
    private Long totalPointsEarned;

    @Schema(description = "Total points used in the period", example = "20000")
    private Long totalPointsUsed;

    @Schema(description = "Net points change (earned - used)", example = "30000")
    private Long netPointsChange;

    @Schema(description = "Total number of point transactions", example = "1500")
    private Long totalTransactions;

    @Schema(description = "Number of users who earned points", example = "500")
    private Long uniqueUsersEarned;

    @Schema(description = "Number of users who used points", example = "200")
    private Long uniqueUsersUsed;

    // === XU HƯỚNG THEO NGÀY ===
    @Schema(description = "Daily trend data")
    private List<DailyPointsData> dailyTrend;

    // === SO SÁNH VỚI KỲ TRƯỚC ===
    @Schema(description = "Comparison with previous period")
    private TrendComparison comparison;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Daily points data")
    public static class DailyPointsData {
        @Schema(description = "Date", example = "2025-01-15")
        private String date;

        @Schema(description = "Points earned on this day", example = "2000")
        private Long pointsEarned;

        @Schema(description = "Points used on this day", example = "800")
        private Long pointsUsed;

        @Schema(description = "Net change on this day", example = "1200")
        private Long netChange;

        @Schema(description = "Number of transactions on this day", example = "50")
        private Long transactionCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Trend comparison with previous period")
    public static class TrendComparison {
        @Schema(description = "Points earned in the previous period", example = "45000")
        private Long previousPeriodEarned;

        @Schema(description = "Points used in the previous period", example = "18000")
        private Long previousPeriodUsed;

        @Schema(description = "% change in points earned vs previous period", example = "11.1")
        private Double earnedChangePercent;

        @Schema(description = "% change in points used vs previous period", example = "11.1")
        private Double usedChangePercent;
    }
}
