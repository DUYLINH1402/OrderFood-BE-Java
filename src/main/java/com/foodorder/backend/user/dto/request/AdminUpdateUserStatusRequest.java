package com.foodorder.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for request to change user status by admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for Admin to update user account status")
public class AdminUpdateUserStatusRequest {

    @Schema(
        description = "Active status (true = active, false = locked)",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Status must not be null")
    private Boolean isActive;

    @Schema(description = "Reason for locking (optional, only when isActive = false)", example = "Account violated platform policy")
    private String reason;
}
