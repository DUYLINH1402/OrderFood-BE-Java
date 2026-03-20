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
@Tag(name = "Users - Super Admin", description = "API quản lý người dùng dành cho Super Admin - Bao gồm dữ liệu được bảo vệ")
public class SuperAdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Danh sách tất cả người dùng",
            description = "Lấy danh sách tất cả người dùng (mọi role), bao gồm cả dữ liệu được bảo vệ.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getAllUsers(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
            @Parameter(description = "Role code (ROLE_USER, ROLE_STAFF, ROLE_ADMIN)") @RequestParam(required = false) String roleCode,
            @Parameter(description = "Trạng thái active") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AdminUserResponse> users = adminUserService.getAllUsers(keyword, roleCode, isActive, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Chi tiết người dùng",
            description = "Xem chi tiết thông tin người dùng, bao gồm cả user được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(
            @Parameter(description = "ID người dùng") @PathVariable Long id) {
        AdminUserResponse user = adminUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Tạo người dùng mới",
            description = "Tạo mới người dùng với bất kỳ role nào.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        AdminUserResponse user = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Cập nhật người dùng",
            description = "Cập nhật thông tin người dùng, bao gồm cả user được bảo vệ và thay đổi role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(
            @Parameter(description = "ID người dùng") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        AdminUserResponse user = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Xóa người dùng",
            description = "Xóa người dùng, bao gồm cả user được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID người dùng") @PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Thay đổi trạng thái người dùng",
            description = "Khóa/mở khóa tài khoản, bao gồm cả user được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @Parameter(description = "ID người dùng") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserStatusRequest request) {
        AdminUserResponse user = adminUserService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Gửi email reset mật khẩu",
            description = "Gửi email để người dùng đặt lại mật khẩu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi email thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> sendResetPasswordEmail(
            @Parameter(description = "ID người dùng") @PathVariable Long id) {
        adminUserService.sendResetPasswordEmail(id);
        return ResponseEntity.ok().build();
    }
}

