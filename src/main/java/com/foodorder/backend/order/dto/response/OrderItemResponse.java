package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing information about an item in the order")
public class OrderItemResponse {

    @Schema(description = "Food item ID", example = "1")
    private Long foodId;

    @Schema(description = "Food item name", example = "Pho bo tai")
    private String foodName;

    @Schema(description = "Food item slug (used for URL)", example = "pho-bo-tai")
    private String foodSlug;

    @Schema(description = "Quantity", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price (VND)", example = "50000")
    private BigDecimal price;

    @Schema(description = "Formatted unit price", example = "55.000đ")
    private String priceFormatted;

    @Schema(description = "Formatted total price", example = "110.000đ")
    private String totalFormatted;
}
