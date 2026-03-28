package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.StaffOrderService;
import com.foodorder.backend.security.annotation.RequireStaff;
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
 * Controller dành riêng cho STAFF quản lý đơn hàng
 * Tập trung vào các thao tác xử lý đơn hàng hàng ngày
 *
 * Đã migrate từ /api/staff/orders → /api/v1/staff/orders (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/staff/orders")
@RequiredArgsConstructor
@Slf4j
@RequireStaff
@Tag(name = "Orders - Staff", description = "Order management API for Staff")
public class StaffOrderController {

    private final StaffOrderService staffOrderService;

    @Operation(summary = "Orders need confirmation", description = "Get list of paid orders waiting for confirmation (PROCESSING status).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No Staff permission")
    })
    @GetMapping("/need-confirmation")
    public ResponseEntity<PageResponse<OrderResponse>> getOrdersNeedConfirmation(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        PageResponse<OrderResponse> orders = staffOrderService.getOrdersNeedConfirmation(pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Processing orders", description = "Get list of orders in processing.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/processing")
    public ResponseEntity<PageResponse<OrderResponse>> getProcessingOrders(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        PageResponse<OrderResponse> orders = staffOrderService.getProcessingOrders(pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Update order status", description = "Update order status by order code (Staff).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Order code") @PathVariable String orderCode,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse updatedOrder = staffOrderService.updateOrderStatusByCode(orderCode, request);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(updatedOrder)
                .build());
    }

    @Operation(summary = "Order details", description = "Get order details by ID or order code.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderIdOrCode}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @Parameter(description = "Order ID or code") @PathVariable String orderIdOrCode) {

        OrderResponse order = staffOrderService.getOrderDetails(orderIdOrCode);

        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .data(order)
                .build());
    }

    @Operation(summary = "Recent orders", description = "Get recent orders with multiple filter options and pagination.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/recent")
    public ResponseEntity<PageResponse<OrderResponse>> getRecentOrders(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "Recent days") @RequestParam(defaultValue = "365") int days,
            @Parameter(description = "Order status") @RequestParam(required = false) String status,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        PageResponse<OrderResponse> orders = staffOrderService.getRecentOrdersWithFilter(
                pageRequest, days, status, search);

        return ResponseEntity.ok(orders);
    }
}
