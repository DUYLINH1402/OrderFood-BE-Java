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
@Tag(name = "Foods - Super Admin", description = "API quản lý món ăn dành cho Super Admin - Bao gồm dữ liệu được bảo vệ")
public class SuperAdminFoodController {

    private final FoodService foodService;
    private final S3Service s3Service;

    // ==================== QUẢN LÝ MÓN ĂN ====================

    @Operation(summary = "Quản lý món ăn (Super Admin)",
            description = "Lấy danh sách tất cả món ăn, bao gồm cả dữ liệu được bảo vệ. Hỗ trợ lọc theo tên, trạng thái, danh mục.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Super Admin")
    })
    @GetMapping("/management")
    public ResponseEntity<Page<FoodResponse>> getFoodsForManagement(
            @Parameter(description = "Tên món ăn (tìm kiếm)") @RequestParam(required = false) String name,
            @Parameter(description = "Trạng thái (AVAILABLE/UNAVAILABLE)") @RequestParam(required = false) String status,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 20) Pageable pageable) {

        FoodFilterRequest filterRequest = FoodFilterRequest.builder()
                .name(name)
                .status(status)
                .categoryId(categoryId)
                .isActive(isActive)
                .build();

        return ResponseEntity.ok(foodService.getFoodsWithFilter(filterRequest, pageable));
    }

    @Operation(summary = "Chi tiết món ăn (Super Admin)",
            description = "Lấy chi tiết món ăn theo ID, bao gồm cả món được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @Parameter(description = "ID của món ăn") @PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @Operation(summary = "Tạo món ăn mới (Super Admin)",
            description = "Tạo món ăn mới với thông tin và hình ảnh.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true)
    public ResponseEntity<FoodResponse> createFood(@ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.createFood(foodRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cập nhật món ăn (Super Admin)",
            description = "Cập nhật thông tin món ăn theo ID, bao gồm cả món được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<FoodResponse> updateFood(
            @Parameter(description = "ID của món ăn") @PathVariable Long id,
            @ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.updateFood(id, foodRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa món ăn (Super Admin)",
            description = "Xóa món ăn theo ID, bao gồm cả món được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @DeleteMapping("/{id}")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<Void> deleteFood(
            @Parameter(description = "ID của món ăn") @PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cập nhật trạng thái món ăn (Super Admin)",
            description = "Cập nhật trạng thái món ăn, bao gồm cả món được bảo vệ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PatchMapping("/{id}/status")
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ADMIN_FOODS_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ADMIN_FOOD_DETAILS_CACHE, key = "#id")
    })
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @Parameter(description = "ID của món ăn") @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload ảnh món ăn (Super Admin)",
            description = "Upload ảnh món ăn lên S3 và trả về URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi upload")
    })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @Parameter(description = "File ảnh cần upload") @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}

