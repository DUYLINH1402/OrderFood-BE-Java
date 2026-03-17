package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller cho User sử dụng Coupon - Yêu cầu đăng nhập
 * Xem coupon khả dụng, validate và áp dụng mã giảm giá
 */
@RestController
@RequestMapping("/api/v1/client/coupons")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coupons - Client", description = "API mã giảm giá dành cho người dùng đã đăng nhập")
public class CouponUserController {

    private final CouponService couponService;

    @Operation(summary = "Coupon khả dụng cho user", description = "Lấy danh sách mã giảm giá mà user hiện tại có thể sử dụng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/available")
    public ResponseEntity<List<CouponResponse>> getAvailableCouponsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<CouponResponse> response = couponService.getAvailableCouponsForUser(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Kiểm tra coupon", description = "Kiểm tra tính hợp lệ của mã giảm giá cho đơn hàng (không áp dụng thực tế).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công - Trả về kết quả kiểm tra")
    })
    @PostMapping("/validate")
    public ResponseEntity<CouponApplyResult> validateCoupon(
            @RequestBody @Valid ApplyCouponRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            try {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (userDetails != null) {
                    Long userId = userDetails.getId();
                    request.setUserId(userId);
                }
            } catch (Exception e) {
                log.error("Error getting userId from CustomUserDetails: {}", e.getMessage());
            }
        }
        CouponApplyResult result = couponService.validateCouponForOrder(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Áp dụng coupon", description = "Áp dụng mã giảm giá vào đơn hàng và tính toán số tiền giảm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công - Trả về kết quả giảm giá"),
            @ApiResponse(responseCode = "400", description = "Coupon không hợp lệ")
    })
    @PostMapping("/apply")
    public ResponseEntity<CouponApplyResult> applyCoupon(@RequestBody @Valid ApplyCouponRequest request) {
        CouponApplyResult result = couponService.applyCouponToOrder(request);
        return ResponseEntity.ok(result);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid argument: " + e.getMessage()));
    }
}

