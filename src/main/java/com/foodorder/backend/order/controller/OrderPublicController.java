package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.OrderRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.service.OrderService;
import com.foodorder.backend.order.config.PaymentConfig;
import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;
import com.foodorder.backend.payments.controller.PaymentController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller public cho đơn hàng - Tạo đơn hàng mới
 * Cho phép khách vãng lai đặt hàng (không bắt buộc đăng nhập)
 *
 * Đã migrate từ POST /api/orders → POST /api/v1/public/orders (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders - Public", description = "API tạo đơn hàng - Công khai")
public class OrderPublicController {

    private final OrderService orderService;
    private final PaymentController paymentController;

    @Operation(summary = "Tạo đơn hàng và thanh toán",
            description = "Tạo đơn hàng mới và khởi tạo link thanh toán dựa trên phương thức được chọn.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo đơn hàng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> createOrderAndPay(@RequestBody OrderRequest orderRequest) {

        // Bước 1: Tạo đơn hàng trước
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        // Bước 2: Lấy payment config dựa trên payment method
        PaymentConfig paymentConfig = PaymentConfig.getPaymentConfig(orderRequest.getPaymentMethod());

        // Bước 3: Tạo payment request với bankCode và embedData đúng
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderResponse.getId());
        paymentRequest.setPaymentMethod(orderRequest.getPaymentMethod().name());
        paymentRequest.setBankCode(paymentConfig.getBankCode());

        // Thêm embedData nếu có (dành cho ATM)
        if (!paymentConfig.getEmbedData().isEmpty()) {
            paymentRequest.setEmbedData(paymentConfig.getEmbedData());
        }

        // Bước 4: Gọi PaymentController để tạo link thanh toán
        PaymentResponse paymentResponse = paymentController.createPayment(paymentRequest);

        return ResponseEntity.ok(paymentResponse);
    }
}

