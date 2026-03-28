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
@Tag(name = "Orders - Client", description = "Order management API - Requires authentication")
public class OrderClientController {

    private final OrderService orderService;

    @Operation(summary = "Get order list", description = "Get current user's orders with pagination and status filter.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @Parameter(description = "Order status (all, pending, confirmed, ...)") @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "Page number (starts from 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "User ID (for testing)") @RequestParam(required = false) Long userId,
            @Parameter(hidden = true) HttpServletRequest request) {

        Long actualUserId = userId != null ? userId : getUserIdFromToken(request);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        PageResponse<OrderResponse> orders = orderService.getOrdersByUser(actualUserId, status, pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order details", description = "Get detailed information of an order by order code.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @Parameter(hidden = true) HttpServletRequest request) {

        Long userId = getUserIdFromToken(request);
        OrderResponse order = orderService.getOrderDetail(orderCode, userId);

        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Update order status", description = "Update order status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status")
    })
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @RequestBody UpdateOrderStatusRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        Long userId = getUserIdFromToken(httpRequest);
        orderService.updateOrderStatus(orderCode, userId, request);

        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully"));
    }

    @Operation(summary = "Cancel order", description = "Cancel an order with specific reason.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot cancel order")
    })
    @PutMapping("/{orderCode}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @RequestBody CancelOrderRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        Long userId = getUserIdFromToken(httpRequest);
        orderService.cancelOrder(orderCode, userId, request.getCancelReason());

        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully"));
    }

    @Operation(summary = "Order statistics", description = "Get order statistics for current user.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
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

