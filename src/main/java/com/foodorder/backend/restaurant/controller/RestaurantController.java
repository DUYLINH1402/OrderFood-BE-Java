package com.foodorder.backend.restaurant.controller;

import com.foodorder.backend.restaurant.dto.RestaurantResponseDTO;
import com.foodorder.backend.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Controller xử lý API public cho thông tin nhà hàng
 */
@RestController
@RequestMapping("/api/v1/public/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "Public API to get restaurant information")
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * Lấy thông tin chi tiết nhà hàng
     * API Public - Không cần đăng nhập
     */
    @Operation(
            summary = "Get restaurant information",
            description = "Get detailed restaurant information including name, address, phone, description, opening hours and gallery images"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant information not found",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<RestaurantResponseDTO> getRestaurantDetails() {
        RestaurantResponseDTO response = restaurantService.getRestaurantDetails();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic())
                .body(response);
    }
}

