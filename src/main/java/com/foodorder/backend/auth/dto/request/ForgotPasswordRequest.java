package com.foodorder.backend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for password reset request")
public class ForgotPasswordRequest {

    @Schema(
        description = "Email of the user requesting password reset",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

}
