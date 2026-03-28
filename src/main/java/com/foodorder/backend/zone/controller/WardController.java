package com.foodorder.backend.zone.controller;

import com.foodorder.backend.zone.dto.response.WardResponse;
import com.foodorder.backend.zone.service.WardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller public cho phường/xã
 *
 * Đã migrate từ /api/wards → /api/v1/public/wards (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/wards")
@RequiredArgsConstructor
@Tag(name = "Wards - Public", description = "Ward management API - Public access")
public class WardController {

    private final WardService wardService;

    @Operation(summary = "Get wards by district",
               description = "Get a list of all wards belonging to a specific district.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "District not found")
    })
    @GetMapping("/by-district/{districtId}")
    public ResponseEntity<List<WardResponse>> getWardsByDistrict(
            @Parameter(description = "District ID", required = true, example = "1")
            @PathVariable Long districtId) {
        List<WardResponse> wards = wardService.getWardsByDistrict(districtId);
        return ResponseEntity.ok(wards);
    }
}
