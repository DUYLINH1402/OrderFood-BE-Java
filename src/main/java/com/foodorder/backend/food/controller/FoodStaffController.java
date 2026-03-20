package com.foodorder.backend.food.controller;

import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.service.FoodService;
import com.foodorder.backend.security.annotation.RequireStaff;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý món ăn dành cho Staff
 * Staff có quyền xem danh sách quản lý và cập nhật trạng thái món ăn
 *
 * Đã migrate từ /api/foods/management, /api/foods/{id}/status → /api/v1/staff/foods (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/staff/foods")
@RequiredArgsConstructor
@RequireStaff
@Tag(name = "Foods - Staff", description = "API quản lý món ăn - Nhân viên")
public class FoodStaffController {

    private final FoodService foodService;

    @Operation(summary = "Quản lý món ăn (Staff)",
            description = "Lấy danh sách món ăn cho Staff quản lý. Hỗ trợ lọc theo tên, trạng thái, danh mục và trạng thái hoạt động.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
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

    @Operation(summary = "Cập nhật trạng thái món ăn (Staff)",
            description = "Cập nhật trạng thái món ăn. Cho phép thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @Parameter(description = "ID của món ăn") @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }
}

