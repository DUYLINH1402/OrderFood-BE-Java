package com.foodorder.backend.user;

import com.foodorder.backend.user.dto.request.ChangePasswordRequest;
import com.foodorder.backend.user.dto.request.UserUpdateRequest;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý thông tin người dùng
 * Yêu cầu đăng nhập
 *
 * Đã migrate từ /api/users → /api/v1/client/users (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/client/users")
@RequiredArgsConstructor
@Tag(name = "Users - Client", description = "User profile management API - Requires authentication")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile", description = "Get the profile of the currently logged-in user, including reward points.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy userId từ CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Lấy lại User từ database với role và rewardPoint được fetch sẵn
        User user = userService.findUserWithRoleAndRewardPointById(userId);

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user profile", description = "Update the profile information of the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PutMapping("/update-profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody @Valid UserUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getId(); // Lấy userId thay vì detached entity

        // Lấy user từ database với role được fetch sẵn
        User user = userService.findUserWithRoleById(userId);
        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setAvatarUrl(request.getAvatarUrl());

        userService.save(user);

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload avatar", description = "Upload a profile picture for the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded successfully - Returns image URL"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getId(); // Đã đúng rồi

        String imageUrl = userService.uploadAvatar(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    @Operation(summary = "Change password", description = "Change the password for the currently logged-in user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "400", description = "Incorrect current password or invalid new password")
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId(); // Lấy userId thay vì detached entity
        
        userService.changePassword(userId, request);
        return ResponseEntity.ok("PASSWORD_CHANGED");
    }


}
