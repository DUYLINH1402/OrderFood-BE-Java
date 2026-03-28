package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO cho thống kê chuyên sâu dành cho StatCards trên Dashboard Admin
 * Bao gồm: Doanh thu thực, Đơn bị hủy, Ghi chú mới, và các chỉ số quan trọng khác
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing overall statistics for Admin Dashboard")
public class AdminDashboardStatsResponse {

    // === REVENUE STATISTICS ===
    @Schema(description = "Actual revenue (only completed orders, paid)", example = "15000000")
    private BigDecimal actualRevenue;

    @Schema(description = "Total revenue (all orders, including unpaid)", example = "20000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Revenue today", example = "500000")
    private BigDecimal revenueToday;

    @Schema(description = "Revenue this week", example = "3500000")
    private BigDecimal revenueThisWeek;

    @Schema(description = "Revenue this month", example = "15000000")
    private BigDecimal revenueThisMonth;

    @Schema(description = "Growth % compared to previous period", example = "15.5")
    private BigDecimal revenueGrowthPercent;

    // === ORDER STATISTICS ===
    @Schema(description = "Total number of orders", example = "500")
    private Long totalOrders;

    @Schema(description = "Orders today", example = "15")
    private Long ordersToday;

    @Schema(description = "Orders this week", example = "80")
    private Long ordersThisWeek;

    @Schema(description = "Orders this month", example = "350")
    private Long ordersThisMonth;

    // === ORDER STATUS STATISTICS ===
    @Schema(description = "Orders pending payment", example = "5")
    private Long pendingOrders;

    @Schema(description = "Orders paid, awaiting confirmation", example = "10")
    private Long processingOrders;

    @Schema(description = "Orders confirmed, being prepared", example = "8")
    private Long confirmedOrders;

    @Schema(description = "Orders being delivered", example = "12")
    private Long deliveringOrders;

    @Schema(description = "Completed orders", example = "450")
    private Long completedOrders;

    @Schema(description = "Cancelled orders", example = "15")
    private Long cancelledOrders;

    @Schema(description = "Orders cancelled today", example = "1")
    private Long cancelledOrdersToday;

    @Schema(description = "Orders cancelled this week", example = "3")
    private Long cancelledOrdersThisWeek;

    @Schema(description = "Cancellation rate (%)", example = "3.0")
    private Double cancellationRate;

    // === INTERNAL NOTES STATISTICS ===
    @Schema(description = "Orders with internal notes", example = "25")
    private Long ordersWithInternalNotes;

    @Schema(description = "New internal notes today", example = "3")
    private Long newInternalNotesToday;

    @Schema(description = "New internal notes this week", example = "10")
    private Long newInternalNotesThisWeek;

    // === REWARD POINTS STATISTICS ===
    @Schema(description = "Total points used", example = "50000")
    private Long totalPointsUsed;

    @Schema(description = "Total discount from points", example = "500000")
    private BigDecimal totalPointsDiscount;

    // === COUPON STATISTICS ===
    @Schema(description = "Orders using coupon", example = "120")
    private Long ordersWithCoupon;

    @Schema(description = "Total discount from coupon", example = "1200000")
    private BigDecimal totalCouponDiscount;

    // === PAYMENT STATISTICS ===
    @Schema(description = "Orders paid", example = "470")
    private Long paidOrders;

    @Schema(description = "Orders unpaid", example = "15")
    private Long unpaidOrders;

    @Schema(description = "Orders refunded", example = "5")
    private Long refundedOrders;

    // === AVERAGE VALUES ===
    @Schema(description = "Average order value", example = "85000")
    private BigDecimal averageOrderValue;
}
