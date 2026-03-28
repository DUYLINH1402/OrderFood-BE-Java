package com.foodorder.backend.payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Response containing order payment status")
public class PaymentStatusResponse {

    @Schema(description = "Order ID", example = "100")
    private Long orderId;

    @Schema(description = "Payment status", example = "PAID", allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED"})
    private String paymentStatus;

    @Schema(description = "Order status", example = "CONFIRMED")
    private String orderStatus;

    @Schema(description = "Payment transaction ID", example = "TXN123456789")
    private String paymentTransactionId;

    @Schema(description = "Payment time", example = "2025-01-20T10:32:00")
    private LocalDateTime paymentTime;
}
