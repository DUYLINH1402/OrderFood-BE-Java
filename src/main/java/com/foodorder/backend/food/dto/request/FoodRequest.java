package com.foodorder.backend.food.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating/updating food item")
public class FoodRequest {

    @Schema(description = "Food name", example = "Beef Pho", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Food description", example = "Beef pho with rich broth")
    private String description;

    @Schema(description = "Food price (VND)", example = "55000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "Food image file", type = "string", format = "binary")
    private MultipartFile imageUrl;

    @Schema(description = "Category ID containing this food", example = "1")
    private Long categoryId;

    @Schema(description = "Parent food ID (if any)", example = "2")
    private Long parentId;

    @Schema(description = "Mark as bestseller", example = "true")
    private Boolean isBestSeller;

    @Schema(description = "Mark as new item", example = "true")
    private Boolean isNew;

    @Schema(description = "Mark as featured", example = "false")
    private Boolean isFeatured;
}
