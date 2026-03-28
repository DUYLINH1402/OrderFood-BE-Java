package com.foodorder.backend.payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response containing payment information")
public class PaymentResponse {

    @Schema(description = "Payment URL to redirect user", example = "https://zalopay.vn/pay/...")
    private String paymentUrl;

    @Schema(description = "Payment gateway", example = "ZALOPAY", allowableValues = {"ZALOPAY", "MOMO", "VNPAY"})
    private String paymentGateway;

    @Schema(description = "Payment status", example = "PENDING", allowableValues = {"PENDING", "SUCCESS", "FAILED"})
    private String status;
}
