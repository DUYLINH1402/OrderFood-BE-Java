package com.foodorder.backend.favorite.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for adding/removing favorite food")
public class FavoriteRequest {

    @Schema(
        description = "Food ID to add to favorites",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long foodId;

    @Schema(
        description = "Food variant ID. Set to null if no variant",
        example = "2"
    )
    private Long variantId;
}
