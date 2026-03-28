package com.foodorder.backend.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho request gửi tin nhắn đến chatbot
 */
@Data
@Schema(description = "Request body for sending a message to the AI chatbot")
public class ChatRequestDTO {

    @Schema(
        description = "Message content to send to chatbot (max 2000 characters)",
        example = "I want to order pho bo",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "MESSAGE_REQUIRED")
    @Size(max = 2000, message = "MESSAGE_MAX_LENGTH_2000")
    private String message;

    @Schema(
        description = "Session ID to maintain conversation context",
        example = "session_abc123"
    )
    private String sessionId;

    @Schema(
        description = "User ID (null for guest users)",
        example = "1"
    )
    private Long userId;

    @Schema(
        description = "Additional user context (location, preferences...)",
        example = "Location: District 1, Preference: Vietnamese food"
    )
    private String userContext;
}
