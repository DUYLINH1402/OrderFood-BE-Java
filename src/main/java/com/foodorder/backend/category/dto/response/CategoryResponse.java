package com.foodorder.backend.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Response containing food category details")
public class CategoryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Main Dishes")
    private String name;

    @Schema(description = "Category description", example = "Main course items on the menu")
    private String description;

    @Schema(description = "Parent category ID. Null for root category", example = "1")
    private Long parentId;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether category has children", example = "true")
    private boolean hasChildren;

    @Schema(description = "Category slug (for URL)", example = "main-dishes")
    private String slug;
}
