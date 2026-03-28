package com.foodorder.backend.food.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO dùng để thay đổi trạng thái món ăn
 * Staff có thể thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
 * Có thể thêm ghi chú lý do hết hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating food status")
public class FoodStatusUpdateRequest {

    @Schema(
        description = "Food status",
        example = "AVAILABLE",
        allowableValues = {"AVAILABLE", "UNAVAILABLE"}
    )
    private String status;

    @Schema(description = "Active status of food", example = "true")
    private Boolean isActive;

    @Schema(
        description = "Note for status change reason (e.g., out of stock, maintenance...)",
        example = "Temporarily out of ingredients, expected back tomorrow"
    )
    private String statusNote;
}
