package com.foodorder.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho request tạo user mới từ admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for Admin to create a new user account")
public class AdminCreateUserRequest {

    @Schema(
        description = "Username (3-50 characters)",
        example = "johndoe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Schema(
        description = "User email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email address")
    private String email;

    @Schema(
        description = "Password (6-100 characters)",
        example = "Password@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(description = "Full name", example = "Nguyen Van A")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Schema(description = "Phone number", example = "0901234567")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Schema(description = "Address", example = "123 Nguyen Hue, District 1, Ho Chi Minh City")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Role code", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"})
    private String roleCode;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Email verified status", example = "true")
    private Boolean isVerified;
}
