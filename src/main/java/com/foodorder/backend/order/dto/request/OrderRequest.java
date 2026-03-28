package com.foodorder.backend.order.dto.request;

import com.foodorder.backend.order.entity.DeliveryType;
import com.foodorder.backend.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating new order")
public class OrderRequest {

    @Schema(description = "User ID placing the order", example = "1")
    private Long userId;

    @Schema(description = "Receiver name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    @Schema(description = "Receiver phone number", example = "0901234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverPhone;

    @Schema(description = "Receiver email (for notifications)", example = "user@example.com")
    private String receiverEmail;

    @Schema(description = "Delivery address", example = "123 Nguyen Hue, District 1, HCMC", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deliveryAddress;

    @Schema(description = "Shipping zone ID", example = "1")
    private Long shippingZoneId;

    @Schema(description = "Payment method", example = "COD", allowableValues = {"COD", "BANK_TRANSFER", "VNPAY"})
    private PaymentMethod paymentMethod;

    @Schema(description = "Delivery type", example = "DELIVERY", allowableValues = {"DELIVERY", "PICKUP"})
    private DeliveryType deliveryType;

    // === NEW CURRENCY - CLEAR ===
    @Schema(description = "Subtotal amount for food items (excluding shipping, before discount)", example = "150000")
    private BigDecimal subtotalAmount;

    @Schema(description = "Shipping fee", example = "15000")
    private BigDecimal shippingFee;

    @Schema(description = "Total amount after adding shipping fee, before applying discount", example = "165000")
    private BigDecimal totalBeforeDiscount;

    @Schema(description = "Final amount customer has to pay (after all discounts)", example = "145000")
    private BigDecimal finalAmount;

    // === DISCOUNT ===
    @Schema(description = "Reward points to use", example = "100")
    private Integer pointsUsed;

    @Schema(description = "Discount amount from reward points (auto calculated)", example = "10000")
    private BigDecimal pointsDiscountAmount;

    @Schema(description = "Coupon code to apply", example = "SUMMER2025")
    private String couponCode;

    @Schema(description = "Discount amount from coupon (auto calculated)", example = "10000")
    private BigDecimal couponDiscountAmount;

    // === DEPRECATED FIELDS - KEPT FOR COMPATIBILITY ===
    @Deprecated
    @Schema(hidden = true)
    private BigDecimal totalPriceBeforeDiscount;

    @Deprecated
    @Schema(hidden = true)
    private BigDecimal totalPrice;

    @Deprecated
    @Schema(hidden = true)
    private Integer discountAmount;

    @Deprecated
    @Schema(hidden = true)
    private BigDecimal originalAmount;

    @Schema(description = "District ID", example = "1")
    private Long districtId;

    @Schema(description = "Ward ID", example = "1")
    private Long wardId;

    @Schema(description = "List of food items in order", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;
}
