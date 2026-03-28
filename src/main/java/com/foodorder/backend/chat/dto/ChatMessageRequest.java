package com.foodorder.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho yêu cầu gửi tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for sending a chat message")
public class ChatMessageRequest {

    @Schema(
        description = "Message content (max 1000 characters)",
        example = "Hello, I need support",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "MESSAGE_REQUIRED")
    @Size(max = 1000, message = "MESSAGE_MAX_LENGTH_1000")
    private String message;

    @Schema(
        description = "JWT authentication token",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "TOKEN_REQUIRED")
    private String token;

    /**
     * ID của tin nhắn mà staff đang phản hồi (chỉ dành cho staff reply)
     */
    @Schema(
        description = "Original message ID that staff is replying to (staff only)",
        example = "msg_123456"
    )
    private String replyToMessageId;

    /**
     * ID của user mà staff muốn gửi tin nhắn riêng (chỉ dành cho staff)
     */
    @Schema(
        description = "Recipient user ID for direct message (staff only)",
        example = "5"
    )
    private Long recipientUserId;

}
