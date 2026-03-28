package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for user login")
public class UserLoginRequest {

    @Schema(
        description = "Username or email",
        example = "user@gmail.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "LOGIN_REQUIRED")
    private String login;

    @Schema(
        description = "Login password (minimum 8 characters, including uppercase, lowercase, number and special character)",
        example = "A123456",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String password;

}
