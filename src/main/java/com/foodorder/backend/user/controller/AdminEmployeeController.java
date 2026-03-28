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
 * Controller cho admin quản lý nhân viên
 * Base URL: /api/admin/employees
 * Nhân viên là user có role ROLE_STAFF
 */
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@RequireAdmin
@Slf4j
@Tag(name = "Admin Employees", description = "Employee management API for Admin")
public class AdminEmployeeController {

    private static final String EMPLOYEE_ROLE_CODE = "ROLE_STAFF";

    private final AdminUserService adminUserService;

    @Operation(summary = "List employees", description = "Get a paginated and filterable list of employees.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllEmployees(
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

        // Lọc theo role ROLE_STAFF
        Page<AdminUserResponse> employees = adminUserService.getUsersByRole(EMPLOYEE_ROLE_CODE, keyword, isActive, pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Employee details", description = "View detailed information of an employee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        AdminUserResponse employee = adminUserService.getUserById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Create employee", description = "Create a new employee account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping
    public ResponseEntity<AdminUserResponse> createEmployee(@Valid @RequestBody AdminCreateUserRequest request) {

        // Luôn set role là ROLE_STAFF cho nhân viên
        request.setRoleCode(EMPLOYEE_ROLE_CODE);

        AdminUserResponse employee = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @Operation(summary = "Update employee", description = "Update employee information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {

        // Không cho phép thay đổi role thông qua API employees
        request.setRoleCode(null);

        AdminUserResponse employee = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Delete employee", description = "Delete an employee account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Employee ID") @PathVariable Long id) {

        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update account status", description = "Lock or unlock an employee account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateEmployeeStatus(
            @Parameter(description = "Employee ID") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        AdminUserResponse employee = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Send password reset email", description = "Send a password reset email to the employee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        adminUserService.sendResetPasswordEmail(id);
        return ResponseEntity.ok().build();
    }
}
