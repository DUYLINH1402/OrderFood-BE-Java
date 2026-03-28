package com.foodorder.backend.food.controller;

import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.service.FoodService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import com.foodorder.backend.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Controller quản lý món ăn dành cho Admin
 * Admin có đầy đủ quyền của Staff và thêm các quyền CRUD món ăn
 *
 * Đã migrate từ /api/admin/foods → /api/v1/admin/foods (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/admin/foods")

@RequireAdmin
@Tag(name = "Foods - Admin", description = "Food management API for Admin - Full CRUD")
public class FoodAdminController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private S3Service s3Service;

    // ==================== STAFF FUNCTIONS (Admin kế thừa từ Staff) ====================

    /**
     * API lấy danh sách món ăn cho Admin quản lý
     * Hỗ trợ phân trang và bộ lọc theo tên, trạng thái, danh mục
     */
    @Operation(summary = "Food management (Admin)",
            description = "Get food list for Admin management. Supports filtering by name, status, category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @GetMapping("/management")
    public ResponseEntity<Page<FoodResponse>> getFoodsForManagement(
            @Parameter(description = "Food name (search)") @RequestParam(required = false) String name,
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

    /**
     * API cập nhật trạng thái món ăn
     * Cho phép thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
     */
    @Operation(summary = "Update food status",
            description = "Update food status (AVAILABLE/UNAVAILABLE) or isActive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @PatchMapping("/{id}/status")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @Parameter(description = "Food ID") @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN ONLY FUNCTIONS ====================

    /**
     * Lấy chi tiết món ăn theo ID (bao gồm cả món không active)
     */
    @Operation(summary = "Get food details (Admin)", description = "Get food details by ID, including inactive foods.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @Parameter(description = "Food ID") @PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    /**
     * Tạo món ăn mới
     * Yêu cầu: name, price, categoryId, image
     */
    @Operation(summary = "Create food", description = "Create new food with information and image.")
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

    /**
     * Cập nhật thông tin món ăn
     */
    @Operation(summary = "Update food", description = "Update food information by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<FoodResponse> updateFood(
            @Parameter(description = "Food ID") @PathVariable Long id,
            @ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.updateFood(id, foodRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa món ăn (soft delete hoặc hard delete tùy implementation)
     */
    @Operation(summary = "Delete food", description = "Delete food by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @DeleteMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<Void> deleteFood(
            @Parameter(description = "Food ID") @PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload ảnh món ăn lên S3
     * Trả về URL của ảnh đã upload
     */
    @Operation(summary = "Upload food image", description = "Upload food image to S3 and return URL.")
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
