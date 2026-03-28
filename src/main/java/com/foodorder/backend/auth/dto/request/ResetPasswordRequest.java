package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for setting a new password")
public class ResetPasswordRequest {

    @Schema(
        description = "Verification token sent via email",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "TOKEN_REQUIRED")
    private String token;

    @Schema(
        description = "New password (minimum 8 characters, including uppercase, lowercase, number and special character)",
        example = "NewPassword@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String newPassword;
}
