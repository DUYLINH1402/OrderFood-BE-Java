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
@Tag(name = "Coupons - Public", description = "API mã giảm giá công khai")
public class CouponPublicController {

    private final CouponService couponService;

    @Operation(summary = "Danh sách coupon công khai", description = "Lấy danh sách mã giảm giá công khai đang hoạt động.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getActivePublicCoupons() {
        List<CouponResponse> response = couponService.getActivePublicCoupons();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Chi tiết coupon (Code)", description = "Lấy thông tin chi tiết mã giảm giá theo mã code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(
            @Parameter(description = "Mã code của coupon") @PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

