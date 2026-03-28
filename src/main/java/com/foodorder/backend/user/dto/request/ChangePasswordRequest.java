package com.foodorder.backend.user.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body to change account password")
public class ChangePasswordRequest {

    @Schema(
        description = "Current password",
        example = "OldPassword@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @ValidPassword
    private String currentPassword;

    @Schema(
        description = "New password (minimum 8 characters, must include uppercase, lowercase, number and special character)",
        example = "NewPassword@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @ValidPassword
    private String newPassword;
}
