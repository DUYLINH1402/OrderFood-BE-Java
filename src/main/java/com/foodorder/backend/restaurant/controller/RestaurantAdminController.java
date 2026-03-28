package com.foodorder.backend.restaurant.controller;

import com.foodorder.backend.restaurant.dto.GalleryRequest;
import com.foodorder.backend.restaurant.dto.RestaurantResponseDTO;
import com.foodorder.backend.restaurant.dto.RestaurantUpdateRequest;
import com.foodorder.backend.restaurant.service.RestaurantService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý API Admin cho quản lý thông tin nhà hàng
 * Yêu cầu quyền ADMIN
 *
 * Đã migrate từ /api/admin/restaurant → /api/v1/admin/restaurant (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/admin/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant - Admin", description = "Restaurant information management API for Admin")
public class RestaurantAdminController {

    private final RestaurantService restaurantService;

    // ==================== RESTAURANT INFO APIs ====================

    /**
     * Lấy thông tin nhà hàng (Admin)
     */
    @Operation(
            summary = "Get restaurant information",
            description = "Get detailed restaurant information for editing"
    )
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> getRestaurantInfo() {
        return ResponseEntity.ok(restaurantService.getRestaurantDetails());
    }

    /**
     * Cập nhật thông tin nhà hàng
     */
    @Operation(
            summary = "Update restaurant information",
            description = "Update restaurant name, address, phone, description, opening hours"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    @PutMapping
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> updateRestaurantInfo(
            @Valid @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurantInfo(request));
    }

    // ==================== GALLERY APIs ====================

    /**
     * Thêm hình ảnh vào gallery
     */
    @Operation(
            summary = "Add gallery image",
            description = "Add a new image to restaurant gallery"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping("/gallery")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> addGalleryImage(
            @Valid @RequestBody GalleryRequest request) {
        return ResponseEntity.ok(restaurantService.addGalleryImage(request));
    }

    /**
     * Cập nhật hình ảnh gallery
     */
    @Operation(
            summary = "Update gallery image",
            description = "Update URL or display order of a gallery image"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @PutMapping("/gallery/{galleryId}")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> updateGalleryImage(
            @PathVariable Long galleryId,
            @Valid @RequestBody GalleryRequest request) {
        return ResponseEntity.ok(restaurantService.updateGalleryImage(galleryId, request));
    }

    /**
     * Xóa hình ảnh khỏi gallery
     */
    @Operation(
            summary = "Delete gallery image",
            description = "Remove an image from restaurant gallery"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @DeleteMapping("/gallery/{galleryId}")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> deleteGalleryImage(@PathVariable Long galleryId) {
        return ResponseEntity.ok(restaurantService.deleteGalleryImage(galleryId));
    }

    /**
     * Sắp xếp lại thứ tự hình ảnh gallery
     */
    @Operation(
            summary = "Reorder gallery",
            description = "Update display order of gallery images. " +
                    "Pass in list of IDs in desired order."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reordered successfully"),
            @ApiResponse(responseCode = "404", description = "One or more images not found")
    })
    @PutMapping("/gallery/reorder")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> reorderGalleryImages(
            @RequestBody List<Long> galleryIds) {
        return ResponseEntity.ok(restaurantService.reorderGalleryImages(galleryIds));
    }
}

