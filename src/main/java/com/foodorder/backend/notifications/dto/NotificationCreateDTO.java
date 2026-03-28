package com.foodorder.backend.notifications.dto;

import com.foodorder.backend.notifications.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho việc tạo thông báo mới
 * Chứa thông tin cần thiết để tạo một thông báo cho User hoặc Staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating new notification")
public class NotificationCreateDTO {

    @Schema(description = "User ID to receive notification (null if sending to staff)", example = "1")
    private Long userId;

    @Schema(description = "Related order ID", example = "100")
    private Long orderId;

    @Schema(
        description = "Order code",
        example = "ORD-20250120-001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Order code cannot be empty")
    private String orderCode;

    @Schema(
        description = "Notification title",
        example = "Order confirmed",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Title cannot be empty")
    private String title;

    @Schema(
        description = "Notification content",
        example = "Your order ORD-20250120-001 has been confirmed and is being prepared",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Notification content cannot be empty")
    private String message;

    @Schema(
        description = "Notification type",
        example = "ORDER_CONFIRMED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Notification type cannot be empty")
    private String type;

    @Schema(description = "Recipient type", example = "USER", allowableValues = {"USER", "STAFF"})
    @Builder.Default
    private Notification.RecipientType recipientType = Notification.RecipientType.USER;

    @Schema(
        description = "Recipient ID",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Recipient ID cannot be empty")
    private Long recipientId;
}
