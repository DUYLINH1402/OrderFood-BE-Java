package com.foodorder.backend.food.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing food variant information (size, topping, etc.)")
public class FoodVariantResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Variant ID", example = "1")
    private Long id;

    @Schema(description = "Variant name", example = "Size L")
    private String name;

    @Schema(description = "Additional price for this variant (VND)", example = "5000")
    private BigDecimal extraPrice;

    @Schema(description = "Whether this is the default variant", example = "true")
    private boolean isDefault;
}