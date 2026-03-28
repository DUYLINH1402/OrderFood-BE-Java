package com.foodorder.backend.favorite.dto.response;

import com.foodorder.backend.favorite.entity.FavoriteFood;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodVariant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing favorite food information")
public class FavoriteFoodResponse {

    @Schema(description = "Food ID", example = "1")
    private Long foodId;

    @Schema(description = "Food name", example = "Beef Pho")
    private String foodName;

    @Schema(description = "Food image URL", example = "https://example.com/pho.jpg")
    private String foodImageUrl;

    @Schema(description = "Food slug (for URL)", example = "beef-pho")
    private String foodSlug;

    @Schema(description = "Variant ID. Null if no variant", example = "2")
    private Long variantId;

    @Schema(description = "Variant name. Null if no variant", example = "Size L")
    private String variantName;

    @Schema(description = "Total price (including variant surcharge)", example = "60000")
    private BigDecimal totalPrice;

    public static FavoriteFoodResponse fromEntity(FavoriteFood favorite) {
        Food food = favorite.getFood();
        FoodVariant variant = favorite.getVariant();

        BigDecimal basePrice = food.getPrice();
        BigDecimal extra = variant != null ? variant.getExtraPrice() : BigDecimal.ZERO;

        return FavoriteFoodResponse.builder()
                .foodId(food.getId())
                .foodName(food.getName())
                .foodImageUrl(food.getImageUrl())
                .foodSlug(food.getSlug())
                .variantId(variant != null ? variant.getId() : null)
                .variantName(variant != null ? variant.getName() : null)
                .totalPrice(basePrice.add(extra))
                .build();
    }

}

