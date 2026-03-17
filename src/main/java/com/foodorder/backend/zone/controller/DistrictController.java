package com.foodorder.backend.zone.controller;

import com.foodorder.backend.zone.dto.response.DistrictResponse;
import com.foodorder.backend.zone.service.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller public cho quận/huyện
 *
 * Đã migrate từ /api/districts → /api/v1/public/districts (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/districts")
@RequiredArgsConstructor
@Tag(name = "Districts - Public", description = "API quản lý quận/huyện - Công khai")
public class DistrictController {

    private final DistrictService districtService;

    @Operation(summary = "Lấy tất cả quận/huyện",
               description = "Lấy danh sách tất cả quận/huyện có hỗ trợ giao hàng.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<List<DistrictResponse>> getAllDistricts() {
        List<DistrictResponse> districts = districtService.getAllDistricts();
        return ResponseEntity.ok(districts);
    }
}
