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
@Tag(name = "Foods - Staff", description = "Food management API - Staff")
public class FoodStaffController {

    private final FoodService foodService;

    @Operation(summary = "Food management (Staff)",
            description = "Get food list for Staff management. Supports filtering by name, status, category and active status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
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

    @Operation(summary = "Update food status (Staff)",
            description = "Update food status. Allows changing status (AVAILABLE/UNAVAILABLE) or isActive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @Parameter(description = "Food ID") @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }
}

