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
@Tag(name = "Points Statistics", description = "API thống kê và quản lý điểm thưởng - Dành cho Admin")
public class PointsStatisticsController {

    private final PointsStatisticsService pointsStatisticsService;

    // ============ THỐNG KÊ TỔNG QUAN ============

    /**
     * API lấy thống kê tổng quan về điểm thưởng
     * GET /api/v1/admin/promotions/points/statistics
     */
    @Operation(summary = "Thống kê tổng quan điểm thưởng",
               description = "Lấy thống kê tổng quan về điểm thưởng trong hệ thống: tổng điểm, điểm đã dùng, tỷ lệ sử dụng...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping("/statistics")
    public ResponseEntity<PointsStatisticsResponse> getOverallStatistics() {
        PointsStatisticsResponse statistics = pointsStatisticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ============ PHÂN TÍCH XU HƯỚNG ============

    /**
     * API phân tích xu hướng điểm thưởng trong khoảng thời gian
     * GET /api/v1/admin/promotions/points/analytics?startDate=...&endDate=...
     */
    @Operation(summary = "Phân tích xu hướng điểm thưởng",
               description = "Phân tích xu hướng tích lũy và sử dụng điểm thưởng trong khoảng thời gian.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ")
    })
    @GetMapping("/analytics")
    public ResponseEntity<PointsTrendAnalyticsResponse> getTrendAnalytics(
            @Parameter(description = "Thời gian bắt đầu (ISO format)", required = true, example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Thời gian kết thúc (ISO format)", required = true, example = "2025-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        PointsTrendAnalyticsResponse analytics = pointsStatisticsService.getTrendAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * API lấy xu hướng điểm theo ngày
     * GET /api/v1/admin/promotions/points/trend?startDate=...&endDate=...
     */
    @Operation(summary = "Xu hướng điểm theo ngày",
               description = "Lấy dữ liệu xu hướng điểm thưởng theo từng ngày trong khoảng thời gian.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/trend")
    public ResponseEntity<List<PointsTrendAnalyticsResponse.DailyPointsData>> getDailyTrend(
            @Parameter(description = "Thời gian bắt đầu (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Thời gian kết thúc (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PointsTrendAnalyticsResponse.DailyPointsData> trend = pointsStatisticsService.getDailyTrend(startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    // ============ BÁO CÁO THEO USER ============

    /**
     * API lấy chi tiết điểm thưởng của một user
     * GET /api/v1/admin/promotions/points/users/{userId}
     */
    @Operation(summary = "Chi tiết điểm thưởng của user",
               description = "Lấy thông tin chi tiết điểm thưởng của một user: số dư, lịch sử giao dịch...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserPointsDetailResponse> getUserPointsDetail(
            @Parameter(description = "ID của user", required = true, example = "1")
            @PathVariable Long userId) {
        UserPointsDetailResponse userDetail = pointsStatisticsService.getUserPointsDetail(userId);
        return ResponseEntity.ok(userDetail);
    }

    /**
     * API lấy top user có điểm thưởng cao nhất
     * GET /api/v1/admin/promotions/points/users/top?limit=10
     */
    @Operation(summary = "Top user có điểm cao nhất",
               description = "Lấy danh sách top user có số điểm thưởng cao nhất trong hệ thống.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/users/top")
    public ResponseEntity<List<TopUserByPointsResponse>> getTopUsersByPoints(
            @Parameter(description = "Số lượng user cần lấy", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<TopUserByPointsResponse> topUsers = pointsStatisticsService.getTopUsersByPoints(limit);
        return ResponseEntity.ok(topUsers);
    }

    /**
     * API lấy danh sách user có điểm với phân trang và filter
     * GET /api/v1/admin/promotions/points/users?minBalance=100
     */
    @Operation(summary = "Danh sách user có điểm",
               description = "Lấy danh sách user có điểm thưởng với phân trang và filter theo số dư tối thiểu.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/users")
    public ResponseEntity<Page<UserPointsDetailResponse>> getUsersWithPoints(
            @Parameter(description = "Số dư điểm tối thiểu", example = "0")
            @RequestParam(defaultValue = "0") int minBalance,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserPointsDetailResponse> users = pointsStatisticsService.getUsersWithPoints(minBalance, pageable);
        return ResponseEntity.ok(users);
    }

    // ============ QUẢN LÝ ĐIỂM (ADMIN) ============

    /**
     * API điều chỉnh điểm cho user
     * POST /api/v1/admin/promotions/points/users/{userId}/adjust
     */
    @Operation(summary = "Điều chỉnh điểm cho user",
               description = "Admin điều chỉnh (cộng/trừ) điểm thưởng cho một user cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Điều chỉnh thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @PostMapping("/users/{userId}/adjust")
    public ResponseEntity<Map<String, Object>> adjustUserPoints(
            @Parameter(description = "ID của user", required = true, example = "1")
            @PathVariable Long userId,
            @RequestBody AdjustPointsRequest request) {
        pointsStatisticsService.adjustUserPoints(userId, request.getAmount(), request.getReason());

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Đã điều chỉnh điểm thành công",
                "userId", userId,
                "adjustedAmount", request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API cộng điểm hàng loạt cho nhiều user
     * POST /api/v1/admin/promotions/points/bulk-add
     */
    @Operation(summary = "Cộng điểm hàng loạt",
               description = "Admin cộng điểm thưởng cho nhiều user cùng lúc (ví dụ: chương trình khuyến mãi).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cộng điểm thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @PostMapping("/bulk-add")
    public ResponseEntity<Map<String, Object>> bulkAddPoints(@RequestBody BulkAddPointsRequest request) {
        pointsStatisticsService.bulkAddPoints(request.getUserIds(), request.getAmount(), request.getReason());

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Đã cộng điểm hàng loạt thành công",
                "totalUsers", request.getUserIds().size(),
                "pointsPerUser", request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    // ============ DASHBOARD TỔNG HỢP ============

    /**
     * API lấy dashboard tổng hợp cho quản lý điểm thưởng
     * GET /api/v1/admin/promotions/points/dashboard
     */
    @Operation(summary = "Dashboard điểm thưởng",
               description = "Lấy dashboard tổng hợp cho quản lý điểm thưởng: thống kê, top users, xu hướng 30 ngày.")
    @ApiResponse(responseCode = "200", description = "Thành công")
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

