package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc Admin hủy đơn hàng kèm lý do chi tiết
 * Lý do sẽ được lưu vào cột cancel_reason
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for Admin to cancel order with reason")
public class AdminCancelOrderRequest {

    @Schema(
        description = "Order cancellation reason (max 500 characters)",
        example = "Customer requested to cancel order",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Cancellation reason cannot be empty")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancelReason;

    @Schema(
        description = "Additional internal note (Admin only, max 2000 characters)",
        example = "Customer called to request cancellation at 10am"
    )
    @Size(max = 2000, message = "Internal note cannot exceed 2000 characters")
    private String internalNote;
}

