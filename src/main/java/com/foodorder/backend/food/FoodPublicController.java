package com.foodorder.backend.food;

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
@Tag(name = "Foods - Public", description = "API xem món ăn - Công khai")
public class FoodPublicController {

    private final FoodService foodService;

    @Operation(summary = "Lấy danh sách món mới", description = "Lấy danh sách các món ăn mới nhất, sắp xếp theo thời gian tạo.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/new")
    public ResponseEntity<Page<FoodResponse>> getNewFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getNewFoods(pageable));
    }

    @Operation(summary = "Lấy danh sách món nổi bật", description = "Lấy danh sách các món ăn được đánh dấu là nổi bật.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/featured")
    public ResponseEntity<Page<FoodResponse>> getFeaturedFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFeaturedFoods(pageable));
    }

    @Operation(summary = "Lấy danh sách món bán chạy", description = "Lấy danh sách các món ăn bán chạy nhất dựa trên số lượng đã bán.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/bestsellers")
    public ResponseEntity<Page<FoodResponse>> getBestSellerFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getBestSellerFoods(pageable));
    }

    @Operation(summary = "Lấy món ăn theo danh mục (ID)", description = "Lấy danh sách món ăn thuộc một danh mục cụ thể theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategoryId(
            @Parameter(description = "ID của danh mục") @PathVariable Long categoryId,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFoodsByCategoryId(categoryId, pageable));
    }

    @Operation(summary = "Lấy món ăn theo danh mục (Slug)", description = "Lấy danh sách món ăn thuộc một danh mục cụ thể theo slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    @GetMapping("/by-category-slug/{slug}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategorySlug(
            @Parameter(description = "Slug của danh mục") @PathVariable String slug,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFoodsByCategorySlug(slug, pageable));
    }

    @Operation(summary = "Chi tiết món ăn (Slug)", description = "Lấy thông tin chi tiết của một món ăn theo slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<FoodResponse> getFoodBySlug(
            @Parameter(description = "Slug của món ăn") @PathVariable String slug) {
        return ResponseEntity.ok(foodService.getFoodBySlug(slug));
    }

    @Operation(summary = "Chi tiết món ăn (ID)", description = "Lấy thông tin chi tiết của một món ăn theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @Parameter(description = "ID của món ăn") @PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @Operation(summary = "Lấy tất cả món ăn", description = "Lấy danh sách tất cả món ăn đang hoạt động, có phân trang.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<FoodResponse>> getAllFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getAllFoods(pageable));
    }
}

