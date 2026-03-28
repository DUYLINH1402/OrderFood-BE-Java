package com.foodorder.backend.order.dto.response;

import com.foodorder.backend.order.entity.DeliveryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing order details")
public class OrderResponse {

    @Schema(description = "Internal order ID", example = "1")
    private Long id;

    @Schema(description = "Order code displayed to customer", example = "ORD-20250120-001")
    private String orderCode;

    @Schema(description = "User ID who placed the order", example = "1")
    private Long userId;

    @Schema(description = "Delivery type", example = "DELIVERY")
    private DeliveryType deliveryType;

    @Schema(description = "Payment method", example = "COD")
    private String paymentMethod;

    @Schema(description = "District ID", example = "1")
    private Long districtId;

    @Schema(description = "District name", example = "District 1")
    private String districtName;

    @Schema(description = "Ward ID", example = "1")
    private Long wardId;

    @Schema(description = "Ward name", example = "Ben Nghe Ward")
    private String wardName;

    @Schema(description = "Delivery address", example = "123 Nguyen Hue, District 1, HCMC")
    private String deliveryAddress;

    @Schema(description = "Receiver name", example = "John Doe")
    private String receiverName;

    @Schema(description = "Receiver phone", example = "0901234567")
    private String receiverPhone;

    @Schema(description = "Receiver email", example = "user@example.com")
    private String receiverEmail;

    @Schema(description = "Order status", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "READY", "SHIPPING", "DELIVERED", "CANCELLED"})
    private String status;

    @Schema(description = "Payment status", example = "PAID", allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED"})
    private String paymentStatus;

    // === NEW CURRENCY - CLEAR ===
    @Schema(description = "Subtotal amount for food items (excluding shipping, before discount)", example = "150000")
    private BigDecimal subtotalAmount;

    @Schema(description = "Shipping fee", example = "15000")
    private BigDecimal shippingFee;

    @Schema(description = "Total amount after adding shipping fee, before applying discount", example = "165000")
    private BigDecimal totalBeforeDiscount;

    @Schema(description = "Final amount customer has to pay", example = "145000")
    private BigDecimal finalAmount;

    // === DISCOUNT ===
    @Schema(description = "Points used", example = "100")
    private Integer pointsUsed;

    @Schema(description = "Discount amount from reward points", example = "10000")
    private BigDecimal pointsDiscountAmount;

    @Schema(description = "Coupon code used", example = "SUMMER2025")
    private String couponCode;

    @Schema(description = "Discount amount from coupon", example = "10000")
    private BigDecimal couponDiscountAmount;

    // === DEPRECATED FIELDS - KEPT FOR COMPATIBILITY ===
    @Deprecated
    @Schema(hidden = true)
    private Integer discountAmount;

    @Schema(description = "Order creation time", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update time", example = "2025-01-20T10:35:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Payment time", example = "2025-01-20T10:32:00")
    private LocalDateTime paymentTime;

    @Schema(description = "Payment transaction ID", example = "TXN123456789")
    private String paymentTransactionId;

    // === MANAGEMENT FIELDS ===
    @Schema(description = "Staff note for customer", example = "Deliver before 12pm")
    private String staffNote;

    @Schema(description = "Internal note (Staff/Admin only)", example = "VIP customer")
    private String internalNote;

    @Schema(description = "Cancellation reason (if any)", example = "Customer requested cancellation")
    private String cancelReason;

    @Schema(description = "Cancellation time", example = "2025-01-20T11:00:00")
    private LocalDateTime cancelledAt;

    @Schema(description = "List of food items in order")
    private List<OrderItemResponse> items;

    // Item response nested within for convenience
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Information of one food item in order")
    public static class OrderItemResponse {
        @Schema(description = "Food ID", example = "1")
        private Long foodId;

        @Schema(description = "Food name", example = "Beef Pho")
        private String foodName;

        @Schema(description = "Food slug", example = "pho-bo-tai")
        private String foodSlug;

        @Schema(description = "Image URL", example = "https://example.com/pho.jpg")
        private String imageUrl;

        @Schema(description = "Quantity", example = "2")
        private Integer quantity;

        @Schema(description = "Unit price", example = "55000")
        private BigDecimal price;
    }
}
