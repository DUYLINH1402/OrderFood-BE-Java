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
@Tag(name = "Wards - Public", description = "API quản lý phường/xã - Công khai")
public class WardController {

    private final WardService wardService;

    @Operation(summary = "Lấy danh sách phường/xã theo quận/huyện",
               description = "Lấy danh sách tất cả phường/xã thuộc một quận/huyện cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy quận/huyện")
    })
    @GetMapping("/by-district/{districtId}")
    public ResponseEntity<List<WardResponse>> getWardsByDistrict(
            @Parameter(description = "ID của quận/huyện", required = true, example = "1")
            @PathVariable Long districtId) {
        List<WardResponse> wards = wardService.getWardsByDistrict(districtId);
        return ResponseEntity.ok(wards);
    }
}
