package com.foodorder.backend.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for adding/updating a food item in the cart")
public class CartRequest {

    @Schema(
        description = "ID of the food item to add to cart",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long foodId;

    @Schema(
        description = "ID of the food variant (size, topping...). Null if no variant",
        example = "2",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long variantId;

    @Schema(
        description = "Quantity to add. Negative to decrease, positive to increase, 0 to remove",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int quantity;
}

