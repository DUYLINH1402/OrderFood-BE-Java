package com.foodorder.backend.notifications.dto;

import com.foodorder.backend.notifications.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho response thông báo
 * Chứa thông tin thông báo trả về cho client (User/Staff)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing notification information")
public class NotificationResponseDTO {

    @Schema(description = "Notification ID", example = "1")
    private Long id;

    @Schema(description = "User ID receiving notification", example = "1")
    private Long userId;

    @Schema(description = "Related order ID", example = "100")
    private Long orderId;

    @Schema(description = "Order code", example = "ORD-20250120-001")
    private String orderCode;

    @Schema(description = "Notification title", example = "Order confirmed")
    private String title;

    @Schema(description = "Notification content", example = "Your order has been confirmed")
    private String message;

    @Schema(description = "Notification type", example = "ORDER_CONFIRMED")
    private String type;

    @Schema(description = "Recipient type", example = "USER", allowableValues = {"USER", "STAFF"})
    private Notification.RecipientType recipientType;

    @Schema(description = "Recipient ID", example = "1")
    private Long recipientId;

    @Schema(description = "Read status", example = "false")
    private Boolean isRead;

    @Schema(description = "Notification creation time", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Notification read time", example = "2025-01-20T10:35:00")
    private LocalDateTime readAt;
}
