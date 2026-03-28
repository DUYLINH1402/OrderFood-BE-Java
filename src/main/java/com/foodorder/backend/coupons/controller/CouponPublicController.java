package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller công khai cho Coupon - Không cần đăng nhập
 * Xem danh sách coupon công khai, tra cứu coupon theo mã
 */
@RestController
@RequestMapping("/api/v1/public/coupons")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coupons - Public", description = "Public API for discount coupons")
public class CouponPublicController {

    private final CouponService couponService;

    @Operation(summary = "Get active public coupons", description = "Get list of active public coupons.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getActivePublicCoupons() {
        List<CouponResponse> response = couponService.getActivePublicCoupons();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get coupon details (Code)", description = "Get coupon details by code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(
            @Parameter(description = "Coupon code") @PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

