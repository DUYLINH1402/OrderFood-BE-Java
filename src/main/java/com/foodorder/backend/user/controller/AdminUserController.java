package com.foodorder.backend.user.controller;

import com.foodorder.backend.user.dto.request.AdminCreateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserStatusRequest;
import com.foodorder.backend.user.dto.response.AdminUserResponse;
import com.foodorder.backend.user.service.AdminUserService;
import com.foodorder.backend.security.annotation.RequireAdmin;
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
 * Controller cho admin quản lý người dùng (khách hàng)
 * Chỉ quản lý user có role ROLE_USER
 *
 * Đã migrate từ /api/admin/users → /api/v1/admin/users (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@RequireAdmin
@Slf4j
@Tag(name = "Users - Admin", description = "Customer management API for Admin")
public class AdminUserController {

    private static final String USER_ROLE_CODE = "ROLE_USER";

    private final AdminUserService adminUserService;

    @Operation(summary = "List customers", description = "Get a paginated and filterable list of customers.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Chỉ lấy user có role ROLE_USER (khách hàng)
        Page<AdminUserResponse> users = adminUserService.getUsersByRole(USER_ROLE_CODE, keyword, isActive, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Customer details", description = "View detailed information of a customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @Parameter(description = "Customer ID") @PathVariable Long id) {

        AdminUserResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create customer", description = "Create a new customer account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {

        // Luôn set role là ROLE_USER cho khách hàng
        request.setRoleCode(USER_ROLE_CODE);

        AdminUserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Update customer", description = "Update customer information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @Parameter(description = "Customer ID") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {

        // Không cho phép thay đổi role thông qua API users
        request.setRoleCode(null);

        AdminUserResponse user = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Delete customer", description = "Delete a customer account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update account status", description = "Lock or unlock a customer account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @Parameter(description = "Customer ID") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        AdminUserResponse user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Send password reset email", description = "Send a password reset email to the customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        adminUserService.sendResetPasswordEmail(id);
        return ResponseEntity.ok().build();
    }
}
