package com.foodorder.backend.order.dto.request;

import com.foodorder.backend.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request cập nhật trạng thái đơn hàng bởi Staff/Admin
 */
@Data
@Schema(description = "Request body for Staff/Admin to update order status")
public class ManagementUpdateStatusRequest {
    
    @Schema(
        description = "New order status",
        example = "CONFIRMED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Status cannot be empty")
    private OrderStatus status;
    
    @Schema(description = "Note for customer", example = "Your order is being prepared")
    private String note;

    @Schema(description = "Internal note (Staff/Admin only)", example = "Confirmed with customer via phone call")
    private String internalNote;

    @Schema(description = "Notify customer or not", example = "true", defaultValue = "true")
    private boolean notifyCustomer = true;
}
