package com.foodorder.backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


/**
 * DTO cho yêu cầu đăng ký chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for WebSocket chat registration")
public class ChatRegistrationRequest {

    @Schema(
        description = "JWT authentication token",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "TOKEN_REQUIRED")
    private String token;
}
