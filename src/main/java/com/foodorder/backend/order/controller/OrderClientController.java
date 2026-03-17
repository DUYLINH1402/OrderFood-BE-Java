package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.request.CancelOrderRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.OrderService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller quản lý đơn hàng cho người dùng đã đăng nhập
 * Xem danh sách, chi tiết, cập nhật trạng thái, hủy đơn hàng
 *
 * Đã migrate từ /api/orders → /api/v1/client/orders (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/client/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders - Client", description = "API quản lý đơn hàng - Yêu cầu đăng nhập")
public class OrderClientController {

    private final OrderService orderService;

    @Operation(summary = "Lấy danh sách đơn hàng", description = "Lấy danh sách đơn hàng của người dùng hiện tại với phân trang và lọc theo trạng thái.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @Parameter(description = "Trạng thái đơn hàng (all, pending, confirmed, ...)") @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "ID người dùng (dùng để test)") @RequestParam(required = false) Long userId,
            @Parameter(hidden = true) HttpServletRequest request) {

        Long actualUserId = userId != null ? userId : getUserIdFromToken(request);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        PageResponse<OrderResponse> orders = orderService.getOrdersByUser(actualUserId, status, pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Chi tiết đơn hàng", description = "Lấy thông tin chi tiết của một đơn hàng theo mã đơn.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @Parameter(hidden = true) HttpServletRequest request) {

        Long userId = getUserIdFromToken(request);
        OrderResponse order = orderService.getOrderDetail(orderCode, userId);

        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái của một đơn hàng.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ")
    })
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @RequestBody UpdateOrderStatusRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        Long userId = getUserIdFromToken(httpRequest);
        orderService.updateOrderStatus(orderCode, userId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công"));
    }

    @Operation(summary = "Hủy đơn hàng", description = "Hủy một đơn hàng với lý do cụ thể.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hủy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể hủy đơn hàng")
    })
    @PutMapping("/{orderCode}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @RequestBody CancelOrderRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        Long userId = getUserIdFromToken(httpRequest);
        orderService.cancelOrder(orderCode, userId, request.getCancelReason());

        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công"));
    }

    @Operation(summary = "Thống kê đơn hàng", description = "Lấy thống kê đơn hàng của người dùng hiện tại.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsResponse> getOrderStatistics(
            @Parameter(hidden = true) HttpServletRequest request) {

        Long userId = getUserIdFromToken(request);
        OrderStatisticsResponse stats = orderService.getOrderStatistics(userId);

        return ResponseEntity.ok(stats);
    }

    // Helper method để lấy userId từ Spring Security Context
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) principal;
                    return userDetails.getId();
                } else {
                    log.warn("Principal is not CustomUserDetails, type: {}",
                            principal != null ? principal.getClass().getSimpleName() : "null");
                }
            } else {
                log.warn("Authentication is null or not authenticated");
            }

            log.warn("No valid authentication context found, using default userId for testing");
            return 1L; // TODO: Remove this in production

        } catch (Exception e) {
            log.error("Error extracting userId from security context: {}", e.getMessage(), e);
            log.warn("Using fallback userId due to error");
            return 1L;
        }
    }
}

