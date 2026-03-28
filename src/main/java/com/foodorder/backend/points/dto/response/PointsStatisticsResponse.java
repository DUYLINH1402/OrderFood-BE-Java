package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.Map;

/**
 * DTO chứa thống kê tổng quan về điểm thưởng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing overall reward points statistics")
public class PointsStatisticsResponse {

    // === TỔNG QUAN HỆ THỐNG ===
    @Schema(description = "Total users with reward points", example = "1500")
    private Long totalUsersWithPoints;

    @Schema(description = "Total points in the system", example = "500000")
    private Long totalPointsInSystem;

    @Schema(description = "Total points earned", example = "800000")
    private Long totalPointsEarned;

    @Schema(description = "Total points used", example = "300000")
    private Long totalPointsUsed;

    @Schema(description = "Total points refunded", example = "10000")
    private Long totalPointsRefunded;

    @Schema(description = "Total points expired", example = "5000")
    private Long totalPointsExpired;

    // === TRUNG BÌNH ===
    @Schema(description = "Average points per user", example = "333.33")
    private Double averagePointsPerUser;

    @Schema(description = "Average points earned per order", example = "50")
    private Double averagePointsEarnedPerOrder;

    @Schema(description = "Average points used per order", example = "100")
    private Double averagePointsUsedPerOrder;

    // === PHÂN BỔ THEO LOẠI ===
    @Schema(description = "Total points by transaction type", example = "{\"EARN\": 800000, \"USE\": 300000, \"REFUND\": 10000}")
    private Map<String, Long> pointsByType;

    @Schema(description = "Number of transactions by type", example = "{\"EARN\": 5000, \"USE\": 2000, \"REFUND\": 100}")
    private Map<String, Long> transactionsByType;

    // === TỶ LỆ ===
    @Schema(description = "Points usage rate (%)", example = "37.5")
    private Double usageRate;

    @Schema(description = "Points retention rate (%)", example = "62.5")
    private Double retentionRate;
}
