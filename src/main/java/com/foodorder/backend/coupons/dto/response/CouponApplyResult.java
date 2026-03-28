package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO cho kết quả áp dụng coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing coupon application result")
public class CouponApplyResult {

    @Schema(description = "Whether coupon was applied successfully", example = "true")
    private Boolean success;

    @Schema(description = "Result message", example = "Coupon applied successfully")
    private String message;

    @Schema(description = "Applied coupon code", example = "SUMMER2025")
    private String couponCode;

    // Thông tin tính toán
    @Schema(description = "Original order amount", example = "150000")
    private Double originalAmount;

    @Schema(description = "Discount amount", example = "30000")
    private Double discountAmount;

    @Schema(description = "Final amount to pay after discount", example = "120000")
    private Double finalAmount;

    @Schema(description = "Amount saved", example = "30000")
    private Double savedAmount;

    // Thông tin coupon
    @Schema(description = "Coupon title", example = "Summer promotion")
    private String couponTitle;

    @Schema(description = "Discount description", example = "20% off")
    private String discountDescription;

    public static CouponApplyResult success(String couponCode, String couponTitle,
                                          Double originalAmount, Double discountAmount) {
        return CouponApplyResult.builder()
            .success(true)
            .message("Coupon applied successfully")
            .couponCode(couponCode)
            .couponTitle(couponTitle)
            .originalAmount(originalAmount)
            .discountAmount(discountAmount)
            .finalAmount(originalAmount - discountAmount)
            .savedAmount(discountAmount)
            .build();
    }

    public static CouponApplyResult failure(String message) {
        return CouponApplyResult.builder()
            .success(false)
            .message(message)
            .build();
    }
}
