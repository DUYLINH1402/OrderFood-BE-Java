package com.foodorder.backend.coupons.dto.request;

import com.foodorder.backend.coupons.entity.DiscountType;
import com.foodorder.backend.coupons.entity.CouponType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho request tạo/sửa coupon
 * Chỉ chứa các trường cần thiết khi client gửi lên để tạo hoặc cập nhật coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating/updating discount coupon")
public class CouponRequest {

    @Schema(
        description = "Coupon code (3-20 characters, must be unique)",
        example = "SUMMER2025",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Coupon code must not be blank")
    @Size(min = 3, max = 20, message = "Coupon code must be between 3 and 20 characters")
    private String code;

    @Schema(description = "Detailed coupon description", example = "Summer 2025 discount for all foods")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Coupon title", example = "Summer Promotion")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Schema(
        description = "Discount type",
        example = "PERCENT",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PERCENT", "FIXED"}
    )
    @NotNull(message = "Discount type must not be null")
    private DiscountType discountType;

    @Schema(
        description = "Discount value (% or amount depending on type). Max 100 for PERCENT",
        example = "20",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Discount value must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100%")
    private Double discountValue;

    // === ĐIỀU KIỆN ÁP DỤNG NÂNG CAO ===
    @Schema(description = "Minimum order amount to apply coupon", example = "100000")
    @DecimalMin(value = "0.0", message = "Min order amount must be non-negative")
    private Double minOrderAmount;

    @Schema(description = "Maximum discount amount (required for PERCENT type)", example = "50000")
    @DecimalMin(value = "0.0", message = "Max discount amount must be non-negative")
    private Double maxDiscountAmount;

    @Schema(description = "Maximum usage per user", example = "1")
    @Min(value = 1, message = "Max usage per user must be at least 1")
    private Integer maxUsagePerUser;

    @Schema(
        description = "Start date and time",
        example = "2025-02-01T00:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Start date must not be null")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @Schema(
        description = "End date and time (must be after startDate)",
        example = "2025-03-01T23:59:59",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "End date must not be null")
    private LocalDateTime endDate;

    @Schema(
        description = "Total maximum usage count",
        example = "100",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Max usage must not be null")
    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsage;

    @Schema(
        description = "Coupon type: PUBLIC (public) or PRIVATE (for specific users)",
        example = "PUBLIC",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PUBLIC", "PRIVATE"}
    )
    @NotNull(message = "Coupon type must not be null")
    private CouponType couponType;

    // === QUAN HỆ VỚI CÁC ENTITY KHÁC ===
    @Schema(description = "List of applicable category IDs (empty = all categories)", example = "[1, 2]")
    private List<Long> applicableCategoryIds;

    @Schema(description = "List of applicable food IDs (empty = all foods)", example = "[1, 2, 3]")
    private List<Long> applicableFoodIds;

    @Schema(description = "List of applicable user IDs (required for PRIVATE type)", example = "[1, 2]")
    private List<Long> applicableUserIds;

    // === VALIDATION CUSTOM ===
    @AssertTrue(message = "End date must be after start date")
    @Schema(hidden = true)
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Max discount amount is required for percentage discount")
    @Schema(hidden = true)
    private boolean isMaxDiscountAmountValidForPercent() {
        if (discountType == DiscountType.PERCENT) {
            return maxDiscountAmount != null && maxDiscountAmount > 0;
        }
        return true;
    }

    @AssertTrue(message = "Applicable user IDs are required for private coupons")
    @Schema(hidden = true)
    private boolean isApplicableUsersValidForPrivate() {
        if (couponType == CouponType.PRIVATE) {
            return applicableUserIds != null && !applicableUserIds.isEmpty();
        }
        return true;
    }
}
