package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa thông tin sử dụng coupon của một User
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing user's coupon usage information")
public class UserCouponUsageResponse {

    // === THÔNG TIN USER ===
    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    // === THỐNG KÊ TỔNG QUAN ===
    @Schema(description = "Total coupons used", example = "10")
    private Long totalCouponsUsed;

    @Schema(description = "Total discount received (VND)", example = "500000")
    private Double totalDiscountReceived;

    @Schema(description = "Average discount per order (VND)", example = "50000")
    private Double averageDiscountPerOrder;

    // === LỊCH SỬ SỬ DỤNG ===
    @Schema(description = "Coupon usage history")
    private List<CouponUsageDetail> usageHistory;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Detail of a single coupon usage")
    public static class CouponUsageDetail {
        @Schema(description = "Usage ID", example = "1")
        private Long usageId;

        @Schema(description = "Coupon ID", example = "5")
        private Long couponId;

        @Schema(description = "Coupon code", example = "SUMMER2025")
        private String couponCode;

        @Schema(description = "Coupon title", example = "Summer Promotion")
        private String couponTitle;

        @Schema(description = "Order ID", example = "100")
        private Long orderId;

        @Schema(description = "Discount amount (VND)", example = "50000")
        private Double discountAmount;

        @Schema(description = "Used at", example = "2025-01-15T14:30:00")
        private LocalDateTime usedAt;
    }
}

