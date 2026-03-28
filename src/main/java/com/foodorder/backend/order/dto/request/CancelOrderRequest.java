package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for customer to cancel order")
public class CancelOrderRequest {

    @Schema(
        description = "Reason for order cancellation",
        example = "I changed my mind and don't want to order anymore"
    )
    private String cancelReason;
}
