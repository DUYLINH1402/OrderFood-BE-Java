package com.foodorder.backend.user.dto;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để quản lý thông tin vai trò người dùng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO containing user role information")
public class UserRoleDto {

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Role code", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"})
    private String roleCode;

    @Schema(description = "Role display name", example = "Customer")
    private String roleName;

    @Schema(description = "Active status", example = "true")
    private boolean isActive;

    @Schema(description = "Email verified status", example = "true")
    private boolean isVerified;

    /**
     * Tạo DTO từ User entity
     */
    public static UserRoleDto fromUser(User user) {
        return UserRoleDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .build();
    }
}
