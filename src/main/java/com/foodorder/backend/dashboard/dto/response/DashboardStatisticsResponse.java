package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO trả về thống kê tổng quan cho Dashboard Admin
 * Bao gồm: tổng khách hàng, doanh thu tháng, đơn hàng hôm nay, số nhân viên
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing general dashboard statistics for Admin")
public class DashboardStatisticsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Total number of customers (ROLE_USER)", example = "1500")
    private Long totalCustomers;

    @Schema(description = "Revenue for current month (VND)", example = "15000000")
    private BigDecimal monthlyRevenue;

    @Schema(description = "Orders for today", example = "25")
    private Long todayOrders;

    @Schema(description = "Total number of staff (ROLE_STAFF)", example = "10")
    private Long totalStaff;

    @Schema(description = "Pending orders count (PENDING + PROCESSING)", example = "5")
    private Long pendingOrders;

    @Schema(description = "Completed orders today", example = "20")
    private Long completedTodayOrders;

    @Schema(description = "Revenue growth rate compared to last month (%)", example = "15.5")
    private Double revenueGrowthPercent;

    @Schema(description = "Customer growth rate compared to last month (%)", example = "8.2")
    private Double customerGrowthPercent;
}

