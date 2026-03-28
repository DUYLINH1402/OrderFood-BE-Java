package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response DTO cho API thống kê tổng quan nâng cao
 * Bao gồm: AOV, Tỷ lệ hủy đơn, Khách hàng mới, Điểm thưởng đã dùng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing advanced statistics for Dashboard")
public class AdvancedStatisticsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Average Order Value (VND)", example = "85000")
    private BigDecimal aov;

    @Schema(description = "AOV change rate compared to previous period (%)", example = "5.5")
    private Double aovChangePercent;

    @Schema(description = "Total orders in period", example = "500")
    private Long totalOrders;

    @Schema(description = "Number of cancelled orders", example = "15")
    private Long cancelledOrders;

    @Schema(description = "Cancellation rate (%)", example = "3.0")
    private Double cancellationRate;

    @Schema(description = "Cancellation rate change compared to previous period (%)", example = "-2.5")
    private Double cancellationRateChangePercent;

    @Schema(description = "Number of new customers in period", example = "50")
    private Long newCustomers;

    @Schema(description = "New customers change rate compared to previous period (%)", example = "10.0")
    private Double newCustomersChangePercent;

    @Schema(description = "Total reward points used in period", example = "5000")
    private Long pointsUsed;

    @Schema(description = "Points used change rate compared to previous period (%)", example = "15.0")
    private Double pointsUsedChangePercent;

    @Schema(description = "Points discount value (VND)", example = "50000")
    private BigDecimal pointsDiscountValue;

    @Schema(description = "Statistics period (7, 30, 90 days)", example = "30")
    private Integer periodDays;
}
