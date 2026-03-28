package com.foodorder.backend.payments.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body for creating payment request")
public class PaymentRequest {

    @Schema(description = "Order ID to be paid", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(
        description = "Payment method",
        example = "ZALOPAY",
        allowableValues = {"ZALOPAY", "MOMO", "VISA", "ATM", "COD"}
    )
    private String paymentMethod;

    @Schema(description = "Bank code (can be null if customer doesn't pre-select)", example = "VCB")
    private String bankCode;

    @Schema(description = "Embedded data for ATM and reward points", example = "{\"bankgroup\":\"ATM\"}")
    private String embedData;

    @Schema(description = "Points to use for order", example = "100")
    private Integer point;

}
