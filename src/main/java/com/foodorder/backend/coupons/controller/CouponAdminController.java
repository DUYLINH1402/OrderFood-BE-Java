package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.request.CouponRequest;
import com.foodorder.backend.coupons.dto.response.*;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.coupons.service.CouponStatisticsService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin Controller cho quản lý Coupon
 * Bao gồm: CRUD, quản lý trạng thái, thống kê, phân tích và campaign
 * Các chức năng dành riêng cho admin/manager
 */
@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Validated
@Slf4j
@RequireAdmin
@Tag(name = "Coupons - Admin", description = "Admin API for coupon management")
public class CouponAdminController {

    private final CouponService couponService;
    private final CouponStatisticsService couponStatisticsService;

    // ============ QUẢN LÝ COUPON CƠ BẢN (CRUD) ============

    /**
     * Tạo mới coupon
     * POST /api/admin/coupons
     */
    @Operation(summary = "Create coupon", description = "Create a new discount coupon.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody @Valid CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật coupon
     * PUT /api/admin/coupons/{id}
     */
    @Operation(summary = "Update coupon", description = "Update coupon information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id,
            @RequestBody @Valid CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa coupon (soft delete)
     * DELETE /api/admin/coupons/{id}
     */
    @Operation(summary = "Delete coupon", description = "Delete coupon (soft delete).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy chi tiết coupon theo ID
     * GET /api/admin/coupons/{id}
     */
    @Operation(summary = "Get coupon details (ID)", description = "Get coupon details by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(
            @Parameter(description = "Coupon ID") @PathVariable Long id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách tất cả coupon với phân trang
     * GET /api/admin/coupons
     */
    @Operation(summary = "Get all coupons", description = "Get all coupons with pagination.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        Page<CouponResponse> response = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách coupon theo trạng thái
     * GET /api/admin/coupons/status/{status}
     */
    @Operation(summary = "Get coupons by status", description = "Get coupons by status.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CouponResponse>> getCouponsByStatus(
            @Parameter(description = "Coupon status") @PathVariable CouponStatus status) {
        List<CouponResponse> response = couponService.getCouponsByStatus(status);
        return ResponseEntity.ok(response);
    }

    // ============ QUẢN LÝ TRẠNG THÁI COUPON ============

    /**
     * Kích hoạt coupon
     * PUT /api/admin/coupons/{id}/activate
     */
    @Operation(summary = "Activate coupon", description = "Activate a coupon.")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateCoupon(@PathVariable Long id) {
        couponService.activateCoupon(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Vô hiệu hóa coupon
     * PUT /api/admin/coupons/{id}/deactivate
     */
    @Operation(summary = "Deactivate coupon", description = "Deactivate a coupon.")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCoupon(@PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật trạng thái coupon hết hạn (manual trigger)
     * PUT /api/admin/coupons/update-expired
     */
    @Operation(summary = "Update expired coupons", description = "Update status of expired coupons.")
    @PutMapping("/update-expired")
    public ResponseEntity<Void> updateExpiredCoupons() {
        couponService.updateExpiredCoupons();
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật trạng thái coupon hết lượt sử dụng (manual trigger)
     * PUT /api/admin/coupons/update-used-out
     */
    @Operation(summary = "Update used-out coupons", description = "Update status of coupons that have reached usage limit.")
    @PutMapping("/update-used-out")
    public ResponseEntity<Void> updateUsedOutCoupons() {
        couponService.updateUsedOutCoupons();
        return ResponseEntity.ok().build();
    }

    // ============ THỐNG KÊ TỔNG QUAN ============

    /**
     * Lấy thống kê tổng quan về coupon
     * GET /api/admin/coupons/statistics
     */
    @Operation(summary = "Get coupon statistics", description = "Get overall statistics for all coupons.")
    @GetMapping("/statistics")
    public ResponseEntity<CouponStatisticsResponse> getOverallStatistics() {
        CouponStatisticsResponse statistics = couponStatisticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Thống kê coupon theo trạng thái (đếm số lượng)
     * GET /api/admin/coupons/statistics/by-status
     */
    @Operation(summary = "Get statistics by status", description = "Count coupons by each status.")
    @GetMapping("/statistics/by-status")
    public ResponseEntity<Map<CouponStatus, Long>> getCouponStatisticsByStatus() {
        Map<CouponStatus, Long> stats = couponService.getCouponStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Top coupon được sử dụng nhiều nhất
     * GET /api/admin/coupons/most-used?limit=10
     */
    @Operation(summary = "Get most used coupons", description = "Get list of most frequently used coupons.")
    @GetMapping("/most-used")
    public ResponseEntity<List<CouponResponse>> getMostUsedCoupons(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        List<CouponResponse> response = couponService.getMostUsedCoupons(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Danh sách coupon sắp hết hạn
     * GET /api/admin/coupons/expiring-soon?days=7
     */
    @Operation(summary = "Get expiring coupons", description = "Get list of coupons expiring within N days.")
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<CouponResponse>> getExpiringSoonCoupons(
            @RequestParam(defaultValue = "7") @Min(1) int days) {
        List<CouponResponse> response = couponService.getExpiringSoonCoupons(days);
        return ResponseEntity.ok(response);
    }

    // ============ PHÂN TÍCH SỬ DỤNG ============

    /**
     * Phân tích việc sử dụng coupon trong khoảng thời gian
     * GET /api/admin/coupons/analytics?startDate=...&endDate=...
     */
    @Operation(summary = "Get usage analytics", description = "Analyze coupon usage within date range.")
    @GetMapping("/analytics")
    public ResponseEntity<CouponUsageAnalyticsResponse> getUsageAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        CouponUsageAnalyticsResponse analytics = couponStatisticsService.getUsageAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Lấy xu hướng sử dụng coupon theo ngày
     * GET /api/admin/coupons/trend?startDate=...&endDate=...
     */
    @Operation(summary = "Get usage trend", description = "Get daily coupon usage trend.")
    @GetMapping("/trend")
    public ResponseEntity<List<CouponUsageAnalyticsResponse.DailyUsageData>> getUsageTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<CouponUsageAnalyticsResponse.DailyUsageData> trend = couponStatisticsService.getUsageTrend(startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    // ============ HIỆU SUẤT COUPON ============

    /**
     * Lấy hiệu suất của một coupon cụ thể
     * GET /api/admin/coupons/{couponId}/performance
     */
    @Operation(summary = "Get coupon performance", description = "Get detailed performance info for a specific coupon.")
    @GetMapping("/{couponId}/performance")
    public ResponseEntity<CouponPerformanceResponse> getCouponPerformance(@PathVariable Long couponId) {
        CouponPerformanceResponse performance = couponStatisticsService.getCouponPerformance(couponId);
        return ResponseEntity.ok(performance);
    }

    /**
     * Lấy top coupon hiệu quả nhất
     * GET /api/admin/coupons/top?criteria=USAGE&limit=10
     * @param criteria: USAGE (số lần dùng) hoặc DISCOUNT (tổng tiền giảm)
     */
    @Operation(summary = "Get top performing coupons", description = "Get top performing coupons by criteria.")
    @GetMapping("/top")
    public ResponseEntity<List<CouponUsageAnalyticsResponse.TopCouponData>> getTopCoupons(
            @RequestParam(defaultValue = "USAGE") String criteria,
            @RequestParam(defaultValue = "10") int limit) {
        List<CouponUsageAnalyticsResponse.TopCouponData> topCoupons = couponStatisticsService.getTopCoupons(criteria, limit);
        return ResponseEntity.ok(topCoupons);
    }

    // ============ BÁO CÁO THEO USER ============

    /**
     * Lấy thống kê sử dụng coupon của một user
     * GET /api/admin/coupons/users/{userId}
     */
    @Operation(summary = "Get user coupon usage", description = "Get detailed coupon usage statistics for a specific user.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserCouponUsageResponse> getUserCouponUsage(@PathVariable Long userId) {
        UserCouponUsageResponse userUsage = couponStatisticsService.getUserCouponUsage(userId);
        return ResponseEntity.ok(userUsage);
    }

    /**
     * Lấy top user sử dụng coupon nhiều nhất
     * GET /api/admin/coupons/users/top?limit=10
     */
    @Operation(summary = "Get top users by coupon usage", description = "Get list of users who use coupons most frequently.")
    @GetMapping("/users/top")
    public ResponseEntity<List<UserCouponUsageResponse>> getTopUsersByCouponUsage(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserCouponUsageResponse> topUsers = couponStatisticsService.getTopUsersByCouponUsage(limit);
        return ResponseEntity.ok(topUsers);
    }

    // ============ LỌC VÀ TÌM KIẾM ============

    /**
     * Lọc danh sách coupon với nhiều tiêu chí
     * GET /api/admin/coupons/filter?status=ACTIVE&type=PUBLIC&keyword=...
     */
    @Operation(summary = "Filter coupons", description = "Filter coupons by multiple criteria.")
    @GetMapping("/filter")
    public ResponseEntity<Page<CouponPerformanceResponse>> filterCoupons(
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) CouponType type,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CouponPerformanceResponse> coupons = couponStatisticsService.filterCoupons(status, type, keyword, pageable);
        return ResponseEntity.ok(coupons);
    }

    // ============ DASHBOARD TỔNG HỢP ============

    /**
     * Dashboard tổng quan coupon
     * GET /api/admin/coupons/dashboard
     */
    @Operation(summary = "Get coupon dashboard", description = "Get comprehensive dashboard for coupon management.")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCouponDashboard() {
        log.info("Admin requested coupon dashboard");

        // Lấy thống kê tổng quan
        CouponStatisticsResponse statistics = couponStatisticsService.getOverallStatistics();

        // Lấy top coupon
        List<CouponUsageAnalyticsResponse.TopCouponData> topByUsage = couponStatisticsService.getTopCoupons("USAGE", 5);
        List<CouponUsageAnalyticsResponse.TopCouponData> topByDiscount = couponStatisticsService.getTopCoupons("DISCOUNT", 5);

        // Lấy xu hướng 30 ngày gần đây
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        List<CouponUsageAnalyticsResponse.DailyUsageData> trend = couponStatisticsService.getUsageTrend(startDate, endDate);

        // Lấy top users
        List<UserCouponUsageResponse> topUsers = couponStatisticsService.getTopUsersByCouponUsage(5);

        // Lấy coupon sắp hết hạn
        List<CouponResponse> expiringSoon = couponService.getExpiringSoonCoupons(7);

        Map<String, Object> dashboard = Map.of(
                "statistics", statistics,
                "topCouponsByUsage", topByUsage,
                "topCouponsByDiscount", topByDiscount,
                "usageTrend30Days", trend,
                "topUsersByCouponUsage", topUsers,
                "expiringSoonCoupons", expiringSoon
        );

        return ResponseEntity.ok(dashboard);
    }

    // ============ ADVANCED REPORTS ============

    /**
     * Báo cáo chi tiết theo khoảng thời gian
     * GET /api/admin/coupons/detailed-report?startDate=2024-01-01&endDate=2024-01-31
     */
    @Operation(summary = "Get detailed report", description = "Get detailed coupon report for date range.")
    @GetMapping("/detailed-report")
    public ResponseEntity<Map<String, Object>> getDetailedReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Admin requested detailed report from {} to {}", startDate, endDate);

        var statistics = couponService.getCouponStatistics();
        var mostUsed = couponService.getMostUsedCoupons(10);

        Map<String, Object> report = Map.of(
                "reportPeriod", Map.of("startDate", startDate, "endDate", endDate),
                "statistics", statistics,
                "topCoupons", mostUsed,
                "generatedAt", java.time.LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(report);
    }

    // ============ INTERNAL API (CHO ORDER SERVICE) ============

    /**
     * Xác nhận sử dụng coupon (được gọi từ Order Service)
     * POST /api/admin/coupons/confirm-usage
     */
    @Operation(summary = "Confirm coupon usage", description = "Internal API to confirm coupon usage from Order Service.")
    @PostMapping("/confirm-usage")
    public ResponseEntity<Void> confirmCouponUsage(
            @RequestParam String couponCode,
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam Double discountAmount) {
        couponService.confirmCouponUsage(couponCode, userId, orderId, discountAmount);
        return ResponseEntity.ok().build();
    }

    /**
     * Hủy sử dụng coupon (khi đơn hàng bị hủy)
     * DELETE /api/admin/coupons/usage/{usageId}
     */
    @Operation(summary = "Cancel coupon usage", description = "Cancel coupon usage when order is cancelled.")
    @DeleteMapping("/usage/{usageId}")
    public ResponseEntity<Void> cancelCouponUsage(@PathVariable Long usageId) {
        couponService.cancelCouponUsage(usageId);
        return ResponseEntity.ok().build();
    }

    // ============ EXCEPTION HANDLING ============

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Admin controller error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "ADMIN_OPERATION_FAILED",
                        "message", e.getMessage()
                ));
    }
}
