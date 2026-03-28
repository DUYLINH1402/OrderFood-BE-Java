package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request cập nhật trạng thái đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating order status")
public class UpdateOrderStatusRequest {

    @Schema(
        description = "New order status",
        example = "CONFIRMED",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "READY", "SHIPPING", "DELIVERED", "CANCELLED"}
    )
    @NotBlank(message = "Order status cannot be empty")
    private String status;

    @Schema(description = "Note for status change", example = "Order confirmed via phone call")
    private String note;

    @Schema(description = "Cancellation reason (required when status = CANCELLED)", example = "Customer requested to cancel order")
    private String cancelReason;
}
