package com.foodorder.backend.user.controller;

import com.foodorder.backend.user.dto.request.AdminCreateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserStatusRequest;
import com.foodorder.backend.user.dto.response.AdminUserResponse;
import com.foodorder.backend.user.service.AdminUserService;
import com.foodorder.backend.security.annotation.RequireSuperAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý người dùng dành cho Super Admin
 * SUPER_ADMIN có toàn quyền trên tất cả user, bao gồm user được bảo vệ (isProtected = true)
 *
 * Prefix: /api/v1/superadmin/users
 */
@RestController
@RequestMapping("/api/v1/superadmin/users")
@RequiredArgsConstructor
@RequireSuperAdmin
@Slf4j
@Tag(name = "Users - Super Admin", description = "User management API for Super Admin - Including protected data")
public class SuperAdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List all users",
            description = "Get a list of all users (any role), including protected data.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Role code (ROLE_USER, ROLE_STAFF, ROLE_ADMIN)") @RequestParam(required = false) String roleCode,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserResponse> users = adminUserService.getAllUsers(keyword, roleCode, isActive, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "User details",
            description = "View detailed information of a user, including protected users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        AdminUserResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create user",
            description = "Create a new user with any role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        AdminUserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Update user",
            description = "Update user information, including protected users and role changes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        AdminUserResponse user = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Delete user",
            description = "Delete a user, including protected users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update user status",
            description = "Lock or unlock an account, including protected users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        AdminUserResponse user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Send password reset email",
            description = "Send a password reset email to the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(
            @Parameter(description = "User ID") @PathVariable Long id) {
        adminUserService.sendResetPasswordEmail(id);
        return ResponseEntity.ok().build();
    }
}
