package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for user registration")
public class UserRegisterRequest {

    @Schema(
        description = "Username (must be unique)",
        example = "johndoe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "USERNAME_REQUIRED")
    private String username;

    @Schema(
        description = "User email (used for verification and notifications)",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @Schema(
        description = "Password (minimum 8 characters, including uppercase, lowercase, number and special character)",
        example = "Password@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String password;

}
