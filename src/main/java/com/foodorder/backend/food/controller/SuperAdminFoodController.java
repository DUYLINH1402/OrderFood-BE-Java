package com.foodorder.backend.food.controller;

import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.service.FoodService;
import com.foodorder.backend.security.annotation.RequireSuperAdmin;
import com.foodorder.backend.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý món ăn dành cho Super Admin
 * SUPER_ADMIN có toàn quyền trên tất cả dữ liệu, bao gồm dữ liệu được bảo vệ (isProtected = true)
 *
 * Prefix: /api/v1/superadmin/foods
 */
@RestController
@RequestMapping("/api/v1/superadmin/foods")
@RequiredArgsConstructor
@RequireSuperAdmin
@Tag(name = "Foods - Super Admin", description = "Food management API for Super Admin - Including protected data")
public class SuperAdminFoodController {

    private final FoodService foodService;
    private final S3Service s3Service;

    // ==================== QUẢN LÝ MÓN ĂN ====================

    @Operation(summary = "Manage foods (Super Admin)",
            description = "Get a list of all food items, including protected data. Supports filtering by name, status, and category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Super Admin permission required")
    })
    @GetMapping("/management")
    public ResponseEntity<Page<FoodResponse>> getFoodsForManagement(
            @Parameter(description = "Food name (search keyword)") @RequestParam(required = false) String name,
            @Parameter(description = "Status (AVAILABLE/UNAVAILABLE)") @RequestParam(required = false) String status,
            @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Pagination info") @PageableDefault(size = 20) Pageable pageable) {

        FoodFilterRequest filterRequest = FoodFilterRequest.builder()
                .name(name)
                .status(status)
                .categoryId(categoryId)
                .isActive(isActive)
                .build();

        return ResponseEntity.ok(foodService.getFoodsWithFilter(filterRequest, pageable));
    }

    @Operation(summary = "Food details (Super Admin)",
            description = "Get food item details by ID, including protected items.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @Parameter(description = "Food item ID") @PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @Operation(summary = "Create food item (Super Admin)",
            description = "Create a new food item with information and images.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true)
    public ResponseEntity<FoodResponse> createFood(@ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.createFood(foodRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update food item (Super Admin)",
            description = "Update food item information by ID, including protected items.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<FoodResponse> updateFood(
            @Parameter(description = "Food item ID") @PathVariable Long id,
            @ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.updateFood(id, foodRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete food item (Super Admin)",
            description = "Delete a food item by ID, including protected items.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @DeleteMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<Void> deleteFood(
            @Parameter(description = "Food item ID") @PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update food status (Super Admin)",
            description = "Update a food item's status, including protected items.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @PatchMapping("/{id}/status")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @Parameter(description = "Food item ID") @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload food image (Super Admin)",
            description = "Upload a food image to S3 and return the URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
