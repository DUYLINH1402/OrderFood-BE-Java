package com.foodorder.backend.favorite;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.favorite.dto.request.FavoriteRequest;
import com.foodorder.backend.favorite.dto.response.FavoriteFoodResponse;
import com.foodorder.backend.favorite.service.FavoriteFoodService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý danh sách món ăn yêu thích của người dùng
 */
@RestController
@RequestMapping("/api/v1/client/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Favorite food list management API - Requires authentication")
public class FavoriteFoodController {

    private final FavoriteFoodService favoriteFoodService;


    @Operation(summary = "Get favorites list", description = "Get list of favorite foods for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<List<FavoriteFoodResponse>> getFavorites(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra xác thực người dùng
        if (userDetails == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }

        Long userId = userDetails.getId();
        List<FavoriteFoodResponse> favorites = favoriteFoodService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @Operation(summary = "Add to favorites", description = "Add a food item to favorites list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Food not found")
    })
    @PostMapping
    public ResponseEntity<?> addFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FavoriteRequest request) {
        // Kiểm tra xác thực người dùng
        if (userDetails == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }

        Long userId = userDetails.getId();
        favoriteFoodService.addToFavorites(userId, request);
        return ResponseEntity.ok("Added to favorites!");
    }

    @Operation(summary = "Remove from favorites", description = "Remove a food item from favorites list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Removed successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping
    public ResponseEntity<?> removeFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FavoriteRequest request) {
        // Kiểm tra xác thực người dùng
        if (userDetails == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }

        Long userId = userDetails.getId();
        favoriteFoodService.removeFromFavorites(userId, request);
        return ResponseEntity.ok("Removed from favorites!");
    }
}
