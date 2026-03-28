package com.foodorder.backend.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Response containing cart item details")
public class CartResponse {

    @Schema(description = "Food item ID", example = "1")
    private Long foodId;

    @Schema(description = "Food item name", example = "Pho Bo Tai")
    private String foodName;

    @Schema(description = "Food image URL", example = "https://example.com/images/pho.jpg")
    private String imageUrl;

    @Schema(description = "Food price (including variant if applicable)", example = "55000")
    private BigDecimal price;

    @Schema(description = "Variant ID. Null if no variant", example = "2")
    private Long variantId;

    @Schema(description = "Variant name (size, topping...). Null if no variant", example = "Size L")
    private String variantName;

    @Schema(description = "Quantity in cart", example = "2")
    private int quantity;

    @Schema(description = "Food slug (for URL)", example = "pho-bo-tai")
    private String slug;

}

