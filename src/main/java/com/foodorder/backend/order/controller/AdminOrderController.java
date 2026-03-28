package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.AdminCancelOrderRequest;
import com.foodorder.backend.order.dto.request.UpdateInternalNoteRequest;
import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.AdminDashboardStatsResponse;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.AdminOrderService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controller dành riêng cho ADMIN quản lý đơn hàng
 * Tập trung vào quản lý tổng thể, thống kê và giám sát
 *
 * Đã migrate từ /api/admin/orders → /api/v1/admin/orders (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@RequireAdmin
@Tag(name = "Orders - Admin", description = "Order management API for Admin - Highest permission")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "All orders", description = "Get all orders with multiple filters (Admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Order status") @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Order code") @RequestParam(required = false) String orderCode,
            @Parameter(description = "Customer name") @RequestParam(required = false) String customerName,
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PageResponse<OrderResponse> orders = adminOrderService.getAllOrdersWithFilters(
                status, orderCode, customerName, startDate, endDate, null, pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Order statistics", description = "Get order statistics overview (Admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics(
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @Parameter(description = "Time period") @RequestParam(required = false) String period) {

        OrderStatisticsResponse statistics = adminOrderService.getOrderStatistics(startDate, endDate, period);

        return ResponseEntity.ok(ApiResponse.<OrderStatisticsResponse>builder()
                .success(true)
                .message("Order statistics retrieved successfully")
                .data(statistics)
                .build());
    }

    @Operation(summary = "Update order status", description = "Update order status with highest permission (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse updatedOrder = adminOrderService.updateOrderStatusWithFullAccess(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(updatedOrder)
                .build());
    }

    @Operation(summary = "Delete order", description = "Delete order (soft delete) - Admin only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        adminOrderService.deleteOrder(orderId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Order deleted successfully")
                .build());
    }

    @Operation(summary = "Restore order", description = "Restore cancelled order (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{orderId}/restore")
    public ResponseEntity<ApiResponse<OrderResponse>> restoreOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        OrderResponse order = adminOrderService.restoreOrder(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order restored successfully")
                .data(order)
                .build());
    }

    @Operation(summary = "Full order details", description = "Get order details with full information (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderFullDetails(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {

        OrderResponse order = adminOrderService.getOrderFullDetails(orderId);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order details retrieved successfully")
                .data(order)
                .build());
    }

    @Operation(summary = "Advanced search", description = "Advanced order search with multiple criteria (Admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/advanced-search")
    public ResponseEntity<PageResponse<OrderResponse>> advancedSearch(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Status") @RequestParam(required = false) String status,
            @Parameter(description = "Customer email") @RequestParam(required = false) String customerEmail,
            @Parameter(description = "Customer phone") @RequestParam(required = false) String customerPhone,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) Double minAmount,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) Double maxAmount,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<OrderResponse> orders = adminOrderService.advancedSearch(
                keyword, status, customerEmail, customerPhone, minAmount, maxAmount, pageRequest);

        return ResponseEntity.ok(orders);
    }

    // ============ CÁC API MỚI CHO ADMIN ============

    @Operation(summary = "Update internal note", description = "Update internal note for order (Admin/Staff only).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/internal-note")
    public ResponseEntity<ApiResponse<OrderResponse>> updateInternalNote(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody UpdateInternalNoteRequest request) {

        log.info("Admin updating internal note for order ID: {}", orderId);
        OrderResponse updatedOrder = adminOrderService.updateInternalNote(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Internal note updated successfully")
                .data(updatedOrder)
                .build());
    }

    @Operation(summary = "Cancel order with reason", description = "Cancel order with detailed reason (Admin).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderWithReason(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Valid @RequestBody AdminCancelOrderRequest request) {

        log.info("Admin cancelling order ID: {} with reason: {}", orderId, request.getCancelReason());
        OrderResponse cancelledOrder = adminOrderService.cancelOrderWithReason(orderId, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(cancelledOrder)
                .build());
    }

    @Operation(summary = "Dashboard statistics", description = "Get detailed statistics for Admin Dashboard (revenue, cancelled orders, etc.).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {

        log.info("Admin retrieving dashboard statistics");
        AdminDashboardStatsResponse stats = adminOrderService.getDashboardStats();

        return ResponseEntity.ok(ApiResponse.<AdminDashboardStatsResponse>builder()
                .success(true)
                .message("Dashboard statistics retrieved successfully")
                .data(stats)
                .build());
    }
}
