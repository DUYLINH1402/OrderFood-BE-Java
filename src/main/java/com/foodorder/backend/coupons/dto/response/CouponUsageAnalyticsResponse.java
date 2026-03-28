package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa phân tích chi tiết về việc sử dụng Coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing detailed coupon usage analytics")
public class CouponUsageAnalyticsResponse {

    // === THÔNG TIN THỜI GIAN ===
    @Schema(description = "Analysis start date", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Analysis end date", example = "2025-01-31T23:59:59")
    private LocalDateTime endDate;

    // === THỐNG KÊ TRONG KHOẢNG THỜI GIAN ===
    @Schema(description = "Number of times used in period", example = "500")
    private Long usageCount;

    @Schema(description = "Total discount amount (VND)", example = "5000000")
    private Double totalDiscountAmount;

    @Schema(description = "Average discount per usage (VND)", example = "10000")
    private Double averageDiscountPerUsage;

    @Schema(description = "Number of unique users", example = "200")
    private Long uniqueUsersCount;

    // === XU HƯỚNG THEO NGÀY ===
    @Schema(description = "Daily usage data")
    private List<DailyUsageData> dailyUsageData;

    // === TOP COUPON ===
    @Schema(description = "Top coupons by usage count")
    private List<TopCouponData> topCouponsByUsage;

    @Schema(description = "Top coupons by total discount")
    private List<TopCouponData> topCouponsByDiscount;

    // === PHÂN TÍCH THEO LOẠI ===
    @Schema(description = "Usage analysis by coupon type")
    private Map<String, UsageByTypeData> usageByType;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Daily coupon usage data")
    public static class DailyUsageData {
        @Schema(description = "Date", example = "2025-01-15")
        private String date;

        @Schema(description = "Number of times used", example = "50")
        private Long usageCount;

        @Schema(description = "Total discount amount (VND)", example = "500000")
        private Double discountAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Top coupon information")
    public static class TopCouponData {
        @Schema(description = "Coupon ID", example = "1")
        private Long couponId;

        @Schema(description = "Coupon code", example = "SUMMER2025")
        private String couponCode;

        @Schema(description = "Title", example = "Summer Promotion")
        private String title;

        @Schema(description = "Number of times used", example = "100")
        private Long usageCount;

        @Schema(description = "Total discount amount (VND)", example = "1000000")
        private Double totalDiscountAmount;

        @Schema(description = "Discount type", example = "PERCENT")
        private String discountType;

        @Schema(description = "Discount value", example = "20")
        private Double discountValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Phân tích sử dụng theo loại coupon")
    public static class UsageByTypeData {
        @Schema(description = "Loại coupon", example = "PUBLIC")
        private String type;

        @Schema(description = "Số lượt sử dụng", example = "300")
        private Long usageCount;

        @Schema(description = "Tổng tiền giảm (VND)", example = "3000000")
        private Double totalDiscountAmount;

        @Schema(description = "Phần trăm so với tổng", example = "60.0")
        private Double percentage;
    }
}

