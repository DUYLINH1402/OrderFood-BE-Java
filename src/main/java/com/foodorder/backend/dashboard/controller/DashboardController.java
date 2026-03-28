package com.foodorder.backend.dashboard.controller;

import com.foodorder.backend.dashboard.dto.response.*;
import com.foodorder.backend.dashboard.service.DashboardService;
import com.foodorder.backend.security.annotation.RequireStaff;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API Dashboard cho Admin/Staff
 * Cung cấp các endpoint thống kê và báo cáo tổng quan
 *
 * Đã migrate từ /api/admin/dashboard → /api/v1/staff/dashboard (2026-03-17)
 * Dùng prefix /api/v1/staff/** vì cả Staff và Admin đều có quyền truy cập
 */
@RestController
@RequestMapping("/api/v1/staff/dashboard")
@RequiredArgsConstructor
@Slf4j
@RequireStaff
@Tag(name = "Dashboard", description = "Dashboard statistics API - Admin/Staff")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get overview statistics", description = "Get overall statistics: total customers, monthly revenue, today's orders, staff count.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsResponse> getStatistics() {
        DashboardStatisticsResponse statistics = dashboardService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get daily revenue", description = "Get revenue data for N days (default 7 days, max 365 days).")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/revenue")
    public ResponseEntity<RevenueDataResponse> getRevenueData(
            @Parameter(description = "Number of days") @RequestParam(defaultValue = "7") int days) {
        RevenueDataResponse revenueData = dashboardService.getRevenueData(days);
        return ResponseEntity.ok(revenueData);
    }

    @Operation(summary = "Get recent activities", description = "Get list of recent activities: new orders, completed orders, cancelled orders, new customers.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/activities")
    public ResponseEntity<RecentActivityResponse> getRecentActivities(
            @Parameter(description = "Number of activities") @RequestParam(defaultValue = "10") int limit) {
        RecentActivityResponse activities = dashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    // ============ ADVANCED STATISTICS ENDPOINTS ============

    @Operation(summary = "Get top selling foods", description = "Get top 5 best-selling foods by time period.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/top-selling-foods")
    public ResponseEntity<TopSellingFoodResponse> getTopSellingFoods(
            @Parameter(description = "Period: 7 (week), 30 (month), 90 (quarter)") @RequestParam(defaultValue = "7") int period) {
        TopSellingFoodResponse response = dashboardService.getTopSellingFoods(period);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get advanced statistics", description = "Get advanced statistics: AOV, cancellation rate, new customers, reward points used.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/advanced-statistics")
    public ResponseEntity<AdvancedStatisticsResponse> getAdvancedStatistics(
            @Parameter(description = "Period: 7 (week), 30 (month), 90 (quarter)") @RequestParam(defaultValue = "7") int period) {
        AdvancedStatisticsResponse response = dashboardService.getAdvancedStatistics(period);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get revenue by category", description = "Get revenue breakdown by food category (top 3 + 'Others').")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/revenue-by-category")
    public ResponseEntity<RevenueByCategoryResponse> getRevenueByCategory(
            @Parameter(description = "Period: 7 (week), 30 (month), 90 (quarter)") @RequestParam(defaultValue = "7") int period) {
        RevenueByCategoryResponse response = dashboardService.getRevenueByCategory(period);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get food performance", description = "Get detailed performance for each food: Orders, Revenue, Rating, Trend.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/food-performance")
    public ResponseEntity<FoodPerformanceResponse> getFoodPerformance(
            @Parameter(description = "Period: 7 (week), 30 (month), 90 (quarter)") @RequestParam(defaultValue = "7") int period,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {
        FoodPerformanceResponse response = dashboardService.getFoodPerformance(period, page, size);
        return ResponseEntity.ok(response);
    }
}
