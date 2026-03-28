package com.foodorder.backend.user.dto.response;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing user information")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Phone number", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Address", example = "123 Nguyen Hue, District 1, Ho Chi Minh City")
    private String address;

    @Schema(description = "JWT token (only returned on login)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Role code", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"})
    private String roleCode;

    @Schema(description = "Role display name", example = "Customer")
    private String roleName;

    @Schema(description = "Active status", example = "true")
    private boolean isActive;

    @Schema(description = "Email verified status", example = "true")
    private boolean isVerified;

    @Schema(description = "Current reward points balance", example = "500")
    private int point;

    @Schema(description = "Last login time", example = "2025-01-20T10:30:00")
    private LocalDateTime lastLogin;

    @Schema(description = "Last updated time", example = "2025-01-20T10:30:00")
    private LocalDateTime updatedAt;

    // Method tiện dụng
    public static UserResponse fromEntity(User user) {
        Integer balance = (user.getRewardPoint() != null) ? user.getRewardPoint().getBalance() : 0;
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .point(balance)
                .lastLogin(user.getLastLogin())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
