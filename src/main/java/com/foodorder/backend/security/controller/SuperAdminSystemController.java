package com.foodorder.backend.security.controller;

import com.foodorder.backend.blog.entity.Blog;
import com.foodorder.backend.blog.entity.BlogCategory;
import com.foodorder.backend.blog.repository.BlogCategoryRepository;
import com.foodorder.backend.blog.repository.BlogRepository;
import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.security.annotation.RequireSuperAdmin;
import com.foodorder.backend.security.dto.ProtectedStatusRequest;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller quản lý hệ thống dành cho Super Admin
 * Bao gồm: Toggle trạng thái bảo vệ (isProtected) cho các entity
 *
 * Prefix: /api/v1/superadmin/system
 */
@RestController
@RequestMapping("/api/v1/superadmin/system")
@RequiredArgsConstructor
@RequireSuperAdmin
@Slf4j
@Tag(name = "System - Super Admin", description = "API quản lý hệ thống dành cho Super Admin - Bảo vệ dữ liệu mẫu")
public class SuperAdminSystemController {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;

    // ==================== TOGGLE PROTECTED STATUS ====================

    @Operation(summary = "Toggle bảo vệ món ăn",
            description = "Bật/tắt trạng thái bảo vệ (isProtected) cho món ăn. Dữ liệu được bảo vệ sẽ không thể bị sửa/xóa bởi ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Super Admin")
    })
    @PatchMapping("/foods/{id}/protected")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.FOOD_DETAIL_CACHE, key = "#id")
    })
    public ResponseEntity<Map<String, Object>> toggleFoodProtected(
            @Parameter(description = "ID của món ăn") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn", "FOOD_NOT_FOUND"));

        food.setIsProtected(request.getIsProtected());
        foodRepository.save(food);

        log.info("Super Admin đã {} bảo vệ cho món ăn ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt", id, food.getName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã " + (Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt") + " bảo vệ cho món ăn: " + food.getName(),
                "id", id,
                "isProtected", food.getIsProtected()
        ));
    }

    @Operation(summary = "Toggle bảo vệ người dùng",
            description = "Bật/tắt trạng thái bảo vệ (isProtected) cho người dùng. Dữ liệu được bảo vệ sẽ không thể bị sửa/xóa bởi ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Super Admin")
    })
    @PatchMapping("/users/{id}/protected")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_USERS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_USER_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<Map<String, Object>> toggleUserProtected(
            @Parameter(description = "ID của người dùng") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        user.setProtected(request.getIsProtected());
        userRepository.save(user);

        log.info("Super Admin đã {} bảo vệ cho user ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt", id, user.getUsername());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã " + (Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt") + " bảo vệ cho user: " + user.getUsername(),
                "id", id,
                "isProtected", user.isProtected()
        ));
    }

    @Operation(summary = "Toggle bảo vệ bài viết",
            description = "Bật/tắt trạng thái bảo vệ (isProtected) cho bài viết. Dữ liệu được bảo vệ sẽ không thể bị sửa/xóa bởi ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài viết"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Super Admin")
    })
    @PatchMapping("/blogs/{id}/protected")
    public ResponseEntity<Map<String, Object>> toggleBlogProtected(
            @Parameter(description = "ID của bài viết") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết", "BLOG_NOT_FOUND"));

        blog.setIsProtected(request.getIsProtected());
        blogRepository.save(blog);

        log.info("Super Admin đã {} bảo vệ cho bài viết ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt", id, blog.getTitle());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã " + (Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt") + " bảo vệ cho bài viết: " + blog.getTitle(),
                "id", id,
                "isProtected", blog.getIsProtected()
        ));
    }

    @Operation(summary = "Toggle bảo vệ danh mục blog",
            description = "Bật/tắt trạng thái bảo vệ (isProtected) cho danh mục blog. Dữ liệu được bảo vệ sẽ không thể bị sửa/xóa bởi ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Super Admin")
    })
    @PatchMapping("/blog-categories/{id}/protected")
    public ResponseEntity<Map<String, Object>> toggleBlogCategoryProtected(
            @Parameter(description = "ID của danh mục blog") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục", "BLOG_CATEGORY_NOT_FOUND"));

        category.setIsProtected(request.getIsProtected());
        blogCategoryRepository.save(category);

        log.info("Super Admin đã {} bảo vệ cho danh mục blog ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt", id, category.getName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã " + (Boolean.TRUE.equals(request.getIsProtected()) ? "bật" : "tắt") + " bảo vệ cho danh mục: " + category.getName(),
                "id", id,
                "isProtected", category.getIsProtected()
        ));
    }
}

