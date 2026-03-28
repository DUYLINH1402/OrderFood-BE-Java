package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Information of one food item in order")
public class OrderItemRequest {

    @Schema(description = "Food ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long foodId;

    @Schema(description = "Quantity", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "Food price (VND). Sent from FE, BE can recalculate if needed", example = "55000")
    private BigDecimal price;
}
