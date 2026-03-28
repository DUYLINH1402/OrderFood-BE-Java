package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO trả về dữ liệu doanh thu theo ngày
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing daily revenue data")
public class RevenueDataResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "List of daily revenue entries")
    private List<DailyRevenue> dailyRevenues;

    @Schema(description = "Total revenue for the period (VND)", example = "5000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Total orders for the period", example = "150")
    private Long totalOrders;

    @Schema(description = "Number of days in statistics", example = "7")
    private Integer days;

    /**
     * DTO con chứa doanh thu của 1 ngày
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Daily revenue data")
    public static class DailyRevenue implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Date", example = "2025-01-20")
        private LocalDate date;

        @Schema(description = "Revenue for that day (VND)", example = "500000")
        private BigDecimal revenue;

        @Schema(description = "Order count for that day", example = "15")
        private Long orderCount;
    }
}
