package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO chứa thông tin hiệu suất của một Coupon cụ thể
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing coupon performance metrics")
public class CouponPerformanceResponse {

    // === THÔNG TIN CƠ BẢN ===
    @Schema(description = "Coupon ID", example = "1")
    private Long couponId;

    @Schema(description = "Coupon code", example = "SUMMER2025")
    private String code;

    @Schema(description = "Coupon title", example = "Summer promotion")
    private String title;

    @Schema(description = "Coupon description", example = "20% off for all dishes")
    private String description;

    @Schema(description = "Discount type", example = "PERCENT")
    private String discountType;

    @Schema(description = "Discount value", example = "20")
    private Double discountValue;

    @Schema(description = "Coupon type", example = "PUBLIC")
    private String couponType;

    @Schema(description = "Coupon status", example = "ACTIVE")
    private String status;

    // === THỜI GIAN ===
    @Schema(description = "Start date", example = "2025-02-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "End date", example = "2025-03-01T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Number of active days", example = "15")
    private Long daysActive;

    @Schema(description = "Number of remaining days", example = "10")
    private Long daysRemaining;

    // === THỐNG KÊ SỬ DỤNG ===
    @Schema(description = "Maximum usage count", example = "100")
    private Integer maxUsage;

    @Schema(description = "Used count", example = "45")
    private Integer usedCount;

    @Schema(description = "Remaining usage count", example = "55")
    private Integer remainingUsage;

    @Schema(description = "Usage rate (%)", example = "45.0")
    private Double usageRate;

    // === HIỆU QUẢ ===
    @Schema(description = "Total discount amount (VND)", example = "1500000")
    private Double totalDiscountAmount;

    @Schema(description = "Average discount amount per usage (VND)", example = "33333")
    private Double averageDiscountAmount;

    @Schema(description = "Number of unique users who used this coupon", example = "30")
    private Long uniqueUsersCount;

    @Schema(description = "Average usage per day", example = "3.0")
    private Double averageUsagePerDay;

    // === ĐIỀU KIỆN ===
    @Schema(description = "Minimum order amount", example = "100000")
    private Double minOrderAmount;

    @Schema(description = "Maximum discount amount", example = "50000")
    private Double maxDiscountAmount;

    @Schema(description = "Maximum usage per user", example = "1")
    private Integer maxUsagePerUser;
}

