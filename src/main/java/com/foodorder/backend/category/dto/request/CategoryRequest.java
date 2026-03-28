package com.foodorder.backend.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body for creating/updating a food category")
public class CategoryRequest {

    @Schema(description = "Category name", example = "Main Dishes", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Category description", example = "Main course items on the menu")
    private String description;

    @Schema(description = "Parent category ID (null for root category)", example = "1")
    private Long parentId;

    @Schema(description = "Display order (lower number displayed first)", example = "1")
    private Integer displayOrder;

    @Schema(description = "Category slug (for URL)", example = "main-dishes")
    private String slug;

}
