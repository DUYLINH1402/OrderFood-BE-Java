package com.foodorder.backend.food.controller;

import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.service.FoodService;
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
 * Controller public cho món ăn - Xem danh sách và chi tiết
 * Không yêu cầu đăng nhập
 *
 * Đã migrate từ GET /api/foods → /api/v1/public/foods (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/foods")
@RequiredArgsConstructor
@Tag(name = "Foods - Public", description = "Public API for food items")
public class FoodPublicController {

    private final FoodService foodService;

    @Operation(summary = "Get new foods", description = "Get list of newest food items, sorted by creation time.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/new")
    public ResponseEntity<Page<FoodResponse>> getNewFoods(
            @Parameter(description = "Pagination info") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getNewFoods(pageable));
    }

    @Operation(summary = "Get featured foods", description = "Get list of featured food items.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/featured")
    public ResponseEntity<Page<FoodResponse>> getFeaturedFoods(
            @Parameter(description = "Pagination info") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFeaturedFoods(pageable));
    }

    @Operation(summary = "Get bestseller foods", description = "Get list of best-selling food items based on total sold.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/bestsellers")
    public ResponseEntity<Page<FoodResponse>> getBestSellerFoods(
            @Parameter(description = "Pagination info") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getBestSellerFoods(pageable));
    }

    @Operation(summary = "Get foods by category (ID)", description = "Get list of food items in a specific category by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategoryId(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Pagination info") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFoodsByCategoryId(categoryId, pageable));
    }

    @Operation(summary = "Get foods by category (Slug)", description = "Get list of food items in a specific category by slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/by-category-slug/{slug}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategorySlug(
            @Parameter(description = "Category slug") @PathVariable String slug,
            @Parameter(description = "Pagination info") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFoodsByCategorySlug(slug, pageable));
    }

    @Operation(summary = "Get food details (Slug)", description = "Get detailed information of a food item by slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<FoodResponse> getFoodBySlug(
            @Parameter(description = "Food slug") @PathVariable String slug) {
        return ResponseEntity.ok(foodService.getFoodBySlug(slug));
    }

    @Operation(summary = "Get food details (ID)", description = "Get detailed information of a food item by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @Parameter(description = "Food ID") @PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @Operation(summary = "Get all foods", description = "Get list of all active food items with pagination.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<FoodResponse>> getAllFoods(
            @Parameter(description = "Pagination info") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getAllFoods(pageable));
    }
}

