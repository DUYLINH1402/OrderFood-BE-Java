package com.foodorder.backend.user.dto.response;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO response cho admin xem thông tin user
 * Bao gồm đầy đủ thông tin hơn so với UserResponse thông thường
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing detailed user information (Admin only)")
public class AdminUserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Phone number", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Address", example = "123 Nguyen Hue, District 1, Ho Chi Minh City")
    private String address;

    @Schema(description = "Role code", example = "ROLE_USER")
    private String roleCode;

    @Schema(description = "Role display name", example = "Customer")
    private String roleName;

    @Schema(description = "Active status", example = "true")
    private boolean isActive;

    @Schema(description = "Email verified status", example = "true")
    private boolean isVerified;

    @Schema(description = "Protected data flag (only SUPER_ADMIN can edit/delete)", example = "false")
    private boolean isProtected;

    @Schema(description = "Current reward points balance", example = "500")
    private Integer point;

    @Schema(description = "Last login time", example = "2025-01-20T10:30:00")
    private LocalDateTime lastLogin;

    @Schema(description = "Account creation time", example = "2024-06-15T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated time", example = "2025-01-20T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Entity sang DTO
     */
    public static AdminUserResponse fromEntity(User user) {
        Integer balance = (user.getRewardPoint() != null) ? user.getRewardPoint().getBalance() : 0;
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .isProtected(user.isProtected())
                .point(balance)
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
