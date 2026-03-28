package com.foodorder.backend.food.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO dùng để lọc danh sách món ăn trong trang quản lý
 * Hỗ trợ lọc theo tên, trạng thái, danh mục, và trạng thái hoạt động
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request params for filtering food list")
public class FoodFilterRequest {

    @Schema(description = "Filter by food name (fuzzy search)", example = "beef")
    private String name;

    @Schema(description = "Filter by food status", example = "AVAILABLE", allowableValues = {"AVAILABLE", "UNAVAILABLE"})
    private String status;

    @Schema(description = "Filter by category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Filter by active status", example = "true")
    private Boolean isActive;
}

