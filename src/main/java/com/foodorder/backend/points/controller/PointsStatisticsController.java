package com.foodorder.backend.points.controller;

import com.foodorder.backend.points.dto.response.*;
import com.foodorder.backend.points.service.PointsStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.foodorder.backend.security.annotation.RequireAdmin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller cho API thống kê và quản lý điểm thưởng (Admin)
 * Cung cấp các endpoint cho admin dashboard để quản lý hiệu quả chương trình điểm thưởng
 */
@RestController
@RequestMapping("/api/v1/admin/promotions/points")
@RequiredArgsConstructor
@Slf4j
@RequireAdmin
@Tag(name = "Points Statistics", description = "Reward points statistics and management API - For Admin")
public class PointsStatisticsController {

    private final PointsStatisticsService pointsStatisticsService;

    // ============ OVERALL STATISTICS ============

    /**
     * API lấy thống kê tổng quan về điểm thưởng
     * GET /api/v1/admin/promotions/points/statistics
     */
    @Operation(summary = "Overall points statistics",
               description = "Get overall statistics about reward points in system: total points, points used, usage rate...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "No access permission")
    })
    @GetMapping("/statistics")
    public ResponseEntity<PointsStatisticsResponse> getOverallStatistics() {
        PointsStatisticsResponse statistics = pointsStatisticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ============ TREND ANALYSIS ============

    /**
     * API phân tích xu hướng điểm thưởng trong khoảng thời gian
     * GET /api/v1/admin/promotions/points/analytics?startDate=...&endDate=...
     */
    @Operation(summary = "Points trend analysis",
               description = "Analyze reward points accumulation and usage trend in time period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping("/analytics")
    public ResponseEntity<PointsTrendAnalyticsResponse> getTrendAnalytics(
            @Parameter(description = "Start time (ISO format)", required = true, example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End time (ISO format)", required = true, example = "2025-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        PointsTrendAnalyticsResponse analytics = pointsStatisticsService.getTrendAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * API lấy xu hướng điểm theo ngày
     * GET /api/v1/admin/promotions/points/trend?startDate=...&endDate=...
     */
    @Operation(summary = "Daily points trend",
               description = "Get daily reward points trend data in time period.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/trend")
    public ResponseEntity<List<PointsTrendAnalyticsResponse.DailyPointsData>> getDailyTrend(
            @Parameter(description = "Start time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PointsTrendAnalyticsResponse.DailyPointsData> trend = pointsStatisticsService.getDailyTrend(startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    // ============ USER REPORTS ============

    /**
     * API lấy chi tiết điểm thưởng của một user
     * GET /api/v1/admin/promotions/points/users/{userId}
     */
    @Operation(summary = "User points details",
               description = "Get detailed reward points information of a user: balance, transaction history...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserPointsDetailResponse> getUserPointsDetail(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        UserPointsDetailResponse userDetail = pointsStatisticsService.getUserPointsDetail(userId);
        return ResponseEntity.ok(userDetail);
    }

    /**
     * API lấy top user có điểm thưởng cao nhất
     * GET /api/v1/admin/promotions/points/users/top?limit=10
     */
    @Operation(summary = "Top users by points",
               description = "Get the list of top users with the highest reward points in the system.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/users/top")
    public ResponseEntity<List<TopUserByPointsResponse>> getTopUsersByPoints(
            @Parameter(description = "Number of users to retrieve", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<TopUserByPointsResponse> topUsers = pointsStatisticsService.getTopUsersByPoints(limit);
        return ResponseEntity.ok(topUsers);
    }

    /**
     * API lấy danh sách user có điểm với phân trang và filter
     * GET /api/v1/admin/promotions/points/users?minBalance=100
     */
    @Operation(summary = "List users with points",
               description = "Get paginated list of users with reward points, filterable by minimum balance.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/users")
    public ResponseEntity<Page<UserPointsDetailResponse>> getUsersWithPoints(
            @Parameter(description = "Minimum points balance", example = "0")
            @RequestParam(defaultValue = "0") int minBalance,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserPointsDetailResponse> users = pointsStatisticsService.getUsersWithPoints(minBalance, pageable);
        return ResponseEntity.ok(users);
    }

    // ============ POINTS MANAGEMENT (ADMIN) ============

    /**
     * API điều chỉnh điểm cho user
     * POST /api/v1/admin/promotions/points/users/{userId}/adjust
     */
    @Operation(summary = "Adjust user points",
               description = "Admin adjusts (add/subtract) reward points for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Points adjusted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Admin permission required")
    })
    @PostMapping("/users/{userId}/adjust")
    public ResponseEntity<Map<String, Object>> adjustUserPoints(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @RequestBody AdjustPointsRequest request) {
        pointsStatisticsService.adjustUserPoints(userId, request.getAmount(), request.getReason());

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Points adjusted successfully",
                "userId", userId,
                "adjustedAmount", request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API cộng điểm hàng loạt cho nhiều user
     * POST /api/v1/admin/promotions/points/bulk-add
     */
    @Operation(summary = "Bulk add points",
               description = "Admin adds reward points to multiple users at once (e.g., for a promotion campaign).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Points added successfully"),
            @ApiResponse(responseCode = "403", description = "Admin permission required")
    })
    @PostMapping("/bulk-add")
    public ResponseEntity<Map<String, Object>> bulkAddPoints(@RequestBody BulkAddPointsRequest request) {
        pointsStatisticsService.bulkAddPoints(request.getUserIds(), request.getAmount(), request.getReason());

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Points added to all users successfully",
                "totalUsers", request.getUserIds().size(),
                "pointsPerUser", request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    // ============ AGGREGATE DASHBOARD ============

    /**
     * API lấy dashboard tổng hợp cho quản lý điểm thưởng
     * GET /api/v1/admin/promotions/points/dashboard
     */
    @Operation(summary = "Points dashboard",
               description = "Get aggregate dashboard for reward points management: statistics, top users, 30-day trend.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getPointsDashboard() {

        // Lấy thống kê tổng quan
        PointsStatisticsResponse statistics = pointsStatisticsService.getOverallStatistics();

        // Lấy top users
        List<TopUserByPointsResponse> topUsers = pointsStatisticsService.getTopUsersByPoints(10);

        // Lấy xu hướng 30 ngày gần đây
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        PointsTrendAnalyticsResponse trendAnalytics = pointsStatisticsService.getTrendAnalytics(startDate, endDate);

        Map<String, Object> dashboard = Map.of(
                "statistics", statistics,
                "topUsersByPoints", topUsers,
                "trendAnalytics30Days", trendAnalytics
        );

        return ResponseEntity.ok(dashboard);
    }

    // ============ DTO CLASSES ============

    @lombok.Data
    public static class AdjustPointsRequest {
        private int amount;
        private String reason;
    }

    @lombok.Data
    public static class BulkAddPointsRequest {
        private List<Long> userIds;
        private int amount;
        private String reason;
    }
}

