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
@Tag(name = "System - Super Admin", description = "System management API for Super Admin - Protected data control")
public class SuperAdminSystemController {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;

    // ==================== TOGGLE PROTECTED STATUS ====================

    @Operation(summary = "Toggle food protection",
            description = "Enable/disable the protected status (isProtected) for a food item. Protected data cannot be modified or deleted by ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Food item not found"),
            @ApiResponse(responseCode = "403", description = "Super Admin permission required")
    })
    @PatchMapping("/foods/{id}/protected")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.FOOD_DETAIL_CACHE, key = "#id")
    })
    public ResponseEntity<Map<String, Object>> toggleFoodProtected(
            @Parameter(description = "Food item ID") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found", "FOOD_NOT_FOUND"));

        food.setIsProtected(request.getIsProtected());
        foodRepository.save(food);

        log.info("Super Admin {} protection for food ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "enabled" : "disabled", id, food.getName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", (Boolean.TRUE.equals(request.getIsProtected()) ? "Protection enabled" : "Protection disabled") + " for food: " + food.getName(),
                "id", id,
                "isProtected", food.getIsProtected()
        ));
    }

    @Operation(summary = "Toggle user protection",
            description = "Enable/disable the protected status (isProtected) for a user. Protected data cannot be modified or deleted by ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Super Admin permission required")
    })
    @PatchMapping("/users/{id}/protected")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_USERS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_USER_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<Map<String, Object>> toggleUserProtected(
            @Parameter(description = "User ID") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "USER_NOT_FOUND"));

        user.setProtected(request.getIsProtected());
        userRepository.save(user);

        log.info("Super Admin {} protection for user ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "enabled" : "disabled", id, user.getUsername());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", (Boolean.TRUE.equals(request.getIsProtected()) ? "Protection enabled" : "Protection disabled") + " for user: " + user.getUsername(),
                "id", id,
                "isProtected", user.isProtected()
        ));
    }

    @Operation(summary = "Toggle blog protection",
            description = "Enable/disable the protected status (isProtected) for a blog post. Protected data cannot be modified or deleted by ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog post not found"),
            @ApiResponse(responseCode = "403", description = "Super Admin permission required")
    })
    @PatchMapping("/blogs/{id}/protected")
    public ResponseEntity<Map<String, Object>> toggleBlogProtected(
            @Parameter(description = "Blog post ID") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found", "BLOG_NOT_FOUND"));

        blog.setIsProtected(request.getIsProtected());
        blogRepository.save(blog);

        log.info("Super Admin {} protection for blog ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "enabled" : "disabled", id, blog.getTitle());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", (Boolean.TRUE.equals(request.getIsProtected()) ? "Protection enabled" : "Protection disabled") + " for blog: " + blog.getTitle(),
                "id", id,
                "isProtected", blog.getIsProtected()
        ));
    }

    @Operation(summary = "Toggle blog category protection",
            description = "Enable/disable the protected status (isProtected) for a blog category. Protected data cannot be modified or deleted by ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Blog category not found"),
            @ApiResponse(responseCode = "403", description = "Super Admin permission required")
    })
    @PatchMapping("/blog-categories/{id}/protected")
    public ResponseEntity<Map<String, Object>> toggleBlogCategoryProtected(
            @Parameter(description = "Blog category ID") @PathVariable Long id,
            @RequestBody ProtectedStatusRequest request) {

        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category not found", "BLOG_CATEGORY_NOT_FOUND"));

        category.setIsProtected(request.getIsProtected());
        blogCategoryRepository.save(category);

        log.info("Super Admin {} protection for blog category ID: {} ({})",
                Boolean.TRUE.equals(request.getIsProtected()) ? "enabled" : "disabled", id, category.getName());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", (Boolean.TRUE.equals(request.getIsProtected()) ? "Protection enabled" : "Protection disabled") + " for category: " + category.getName(),
                "id", id,
                "isProtected", category.getIsProtected()
        ));
    }
}

