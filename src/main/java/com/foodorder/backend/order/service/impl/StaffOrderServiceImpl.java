package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderItem;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.entity.PaymentStatus;
import com.foodorder.backend.order.repository.OrderItemRepository;
import com.foodorder.backend.order.service.OrderCoreService;
import com.foodorder.backend.order.service.StaffOrderService;
import com.foodorder.backend.order.util.OrderMapper;
import com.foodorder.backend.service.BrevoEmailService;
import com.foodorder.backend.service.WebSocketService;
import com.foodorder.backend.notifications.service.NotificationHelper;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.util.VnCurrencyFormatter;
import com.foodorder.backend.zone.repository.WardRepository;
import com.foodorder.backend.zone.repository.DistrictRepository;
import com.foodorder.backend.zone.entity.Ward;
import com.foodorder.backend.zone.entity.District;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;;

/**
 * Staff Order Service Implementation
 * Sử dụng OrderCoreService cho logic chung và thêm logic riêng cho Staff
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StaffOrderServiceImpl implements StaffOrderService {

    private final OrderCoreService orderCoreService;
    private final OrderMapper orderMapper;
    private final WebSocketService webSocketService;
    private final WardRepository wardRepository;
    private final DistrictRepository districtRepository;
    private final NotificationHelper notificationHelper;
    private final UserRepository userRepository;
    private final BrevoEmailService brevoEmailService;
    private final TemplateEngine templateEngine;
    private final OrderItemRepository orderItemRepository;

    @Override
    public PageResponse<OrderResponse> getOrdersNeedConfirmation(PageRequest pageRequest) {

        // Tạo spec cho đơn hàng cần xác nhận (PROCESSING - đã thanh toán, chờ xác nhận)
        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.PROCESSING)
        );

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    @Override
    public PageResponse<OrderResponse> getProcessingOrders(PageRequest pageRequest) {
        // Tạo spec cho đơn hàng đang xử lý (CONFIRMED và DELIVERING)
        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.or(
                cb.equal(root.get("status"), OrderStatus.CONFIRMED),   // Đang chế biến
                cb.equal(root.get("status"), OrderStatus.DELIVERING)   // Đang giao
            )
        );

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }


    @Override
    @Transactional
    public OrderResponse updateOrderStatusByCode(String orderCode, UpdateOrderStatusRequest request) {
        // Tìm đơn hàng theo orderCode
        Order order = orderCoreService.findOrderByCode(orderCode);
        String oldStatus = order.getStatus().toString();

        // Lấy trạng thái được phép cho Staff
        Set<OrderStatus> allowedStatuses = orderCoreService.getAllowedStatusesForRole("ROLE_STAFF");

        Order updatedOrder = orderCoreService.updateOrderStatusWithValidation(
            order.getId(), request, allowedStatuses);

        // **GỬI THÔNG BÁO WEBSOCKET VÀ LUU VÀO DATABASE CHO USER**
        try {
            // Chỉ gửi thông báo cho customer, không gửi cho staff để đơn giản hóa
            if (updatedOrder.getUserId() != null) {
                try {
                    // Gửi WebSocket notification cho customer
                    OrderWebSocketMessage customerMessage = OrderWebSocketMessage.customerNotification(
                        updatedOrder.getId(),
                        updatedOrder.getOrderCode(),
                        updatedOrder.getStatus().toString(),
                        oldStatus,
                        updatedOrder.getUserId()
                    );
                    webSocketService.sendNotificationToUser(updatedOrder.getUserId(), customerMessage);

                    // Lưu thông báo vào database cho customer
                    createOrderStatusNotificationForUser(updatedOrder, oldStatus);

                    // Gửi email thông báo khi đơn hàng COMPLETED hoặc CANCELLED
                    if (updatedOrder.getStatus() == OrderStatus.COMPLETED
                            || updatedOrder.getStatus() == OrderStatus.CANCELLED) {
                        sendOrderStatusEmail(updatedOrder);
                    }

                } catch (Exception userNotificationEx) {
                    log.error("Lỗi khi gửi thông báo cho user {} về đơn hàng {}: {}",
                            updatedOrder.getUserId(), updatedOrder.getOrderCode(), userNotificationEx.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo cho đơn hàng {}: {}",
                    updatedOrder.getOrderCode(), e.getMessage());
        }

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse getOrderDetails(String orderIdOrCode) {
        // Tự động phát hiện và xử lý theo ID hoặc mã đơn hàng
        Order order;
        try {
            // Thử parse thành Long trước (nếu là ID)
            Long orderId = Long.parseLong(orderIdOrCode);
            order = orderCoreService.findOrderByIdWithValidation(orderId);
        } catch (NumberFormatException e) {
            // Nếu không parse được thành Long, coi như là orderCode
            order = orderCoreService.findOrderByCode(orderIdOrCode);
        }
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getRecentOrders(PageRequest pageRequest) {
        // Lấy đơn hàng trong 7 ngày gần đây (loại trừ PENDING - chỉ Admin mới thấy)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), sevenDaysAgo),
                cb.notEqual(root.get("status"), OrderStatus.PENDING)  // Loại trừ PENDING
            )
        );

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    @Override
    public PageResponse<OrderResponse> getRecentOrdersWithFilter(PageRequest pageRequest, int days, String status, String search) {

        // Tính ngày bắt đầu dựa trên số ngày được chỉ định
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        // Tạo Specification cơ bản - lấy đơn hàng trong khoảng thời gian và loại trừ PENDING
        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), startDate),
                cb.notEqual(root.get("status"), OrderStatus.PENDING)  // Staff không thấy đơn PENDING
            )
        );

        // Thêm bộ lọc theo trạng thái nếu có
        if (status != null && !status.trim().isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), orderStatus)
                );
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
            }
        }

        // Thêm bộ lọc tìm kiếm nếu có
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("orderCode")), searchTerm),
                cb.like(cb.lower(root.get("receiverName")), searchTerm),
                cb.like(cb.lower(root.get("receiverPhone")), searchTerm),
                cb.like(cb.lower(root.get("receiverEmail")), searchTerm)
            ));
        }

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    // ============ PRIVATE HELPER METHODS FOR NOTIFICATIONS ============

    /**
     * Gửi email thông báo trạng thái đơn hàng (COMPLETED hoặc CANCELLED)
     * Chạy bất đồng bộ để không block luồng chính
     */
    @Async("taskExecutor")
    public void sendOrderStatusEmail(Order order) {
        try {
            // Lấy email người nhận từ đơn hàng hoặc từ user
            String recipientEmail = order.getReceiverEmail();
            String recipientName = order.getReceiverName();

            // Nếu không có email người nhận, thử lấy từ user
            if (recipientEmail == null || recipientEmail.isEmpty()) {
                if (order.getUserId() != null) {
                    User user = userRepository.findById(order.getUserId()).orElse(null);
                    if (user != null && user.getEmail() != null) {
                        recipientEmail = user.getEmail();
                        recipientName = user.getFullName() != null ? user.getFullName() : user.getEmail();
                    }
                }
            }

            if (recipientEmail == null || recipientEmail.isEmpty()) {
                log.warn("Không tìm thấy email để gửi thông báo cho đơn hàng {}", order.getOrderCode());
                return;
            }

            // Tạo context chung cho Thymeleaf
            Context context = buildOrderEmailContext(order, recipientName);

            String subject;
            String templateName;

            if (order.getStatus() == OrderStatus.COMPLETED) {
                subject = String.format(" Đơn hàng #%s đã giao thành công!", order.getOrderCode());
                templateName = "order_completed_email";
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                subject = String.format(" Đơn hàng #%s đã bị hủy", order.getOrderCode());
                templateName = "order_cancelled_email";

                // Thêm thông tin riêng cho đơn hủy
                context.setVariable("cancelReason",
                        order.getCancelReason() != null ? order.getCancelReason() : "");
                context.setVariable("isPaid",
                        order.getPaymentStatus() == PaymentStatus.PAID);
            } else {
                return;
            }

            String htmlContent = templateEngine.process(templateName, context);
            brevoEmailService.sendEmail(recipientEmail, subject, htmlContent);

            log.info("Đã gửi email thông báo {} cho {} về đơn hàng {}",
                    order.getStatus(), recipientEmail, order.getOrderCode());

        } catch (Exception e) {
            log.error("Lỗi khi gửi email thông báo trạng thái đơn hàng {}: {}",
                    order.getOrderCode(), e.getMessage(), e);
        }
    }

    /**
     * Tạo Thymeleaf Context chung cho email đơn hàng (COMPLETED / CANCELLED)
     */
    private Context buildOrderEmailContext(Order order, String recipientName) {
        Context context = new Context();

        // Thông tin cơ bản
        context.setVariable("fullName", recipientName != null ? recipientName : "Quý khách");
        context.setVariable("orderCode", order.getOrderCode());

        // Danh sách sản phẩm
        List<Map<String, Object>> orderItemsList = new ArrayList<>();
        List<OrderItem> items = orderItemRepository.findByOrderIdWithFood(order.getId());
        for (OrderItem item : items) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("foodName", item.getFoodName() != null ? item.getFoodName() :
                    (item.getFood() != null ? item.getFood().getName() : ""));
            itemMap.put("quantity", item.getQuantity() != null ? item.getQuantity() : 0);

            long price = item.getPrice() != null ? item.getPrice().longValue() : 0;
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            long total = price * quantity;

            itemMap.put("priceFormatted", VnCurrencyFormatter.format(price));
            itemMap.put("totalFormatted", VnCurrencyFormatter.format(total));

            orderItemsList.add(itemMap);
        }
        context.setVariable("orderItems", orderItemsList);

        // Tổng tiền - sử dụng field mới
        long subtotal = order.getSubtotalAmount() != null ? order.getSubtotalAmount().longValue() : 0;
        long shippingFee = order.getShippingFee() != null ? order.getShippingFee().longValue() : 0;
        long finalAmount = order.getFinalAmount() != null ? order.getFinalAmount().longValue() : 0;

        // Điểm đã dùng và giảm giá coupon (chi tiết riêng biệt)
        long pointsDiscount = order.getPointsDiscountAmount() != null ? order.getPointsDiscountAmount().longValue() : 0;
        long couponDiscount = order.getCouponDiscountAmount() != null ? order.getCouponDiscountAmount().longValue() : 0;
        int pointsUsed = order.getPointsUsed() != null ? order.getPointsUsed() : 0;

        context.setVariable("subtotalFormatted", VnCurrencyFormatter.format(subtotal));
        context.setVariable("shippingFee", shippingFee);
        context.setVariable("shippingFeeFormatted", VnCurrencyFormatter.format(shippingFee));
        context.setVariable("pointsUsed", pointsUsed);
        context.setVariable("pointsDiscount", pointsDiscount);
        context.setVariable("pointsDiscountFormatted", VnCurrencyFormatter.format(pointsDiscount));
        context.setVariable("couponDiscount", couponDiscount);
        context.setVariable("couponDiscountFormatted", VnCurrencyFormatter.format(couponDiscount));
        context.setVariable("couponCode", order.getCouponCode() != null ? order.getCouponCode() : "");
        context.setVariable("finalAmountFormatted", VnCurrencyFormatter.format(finalAmount));

        // Thông tin thanh toán - hiển thị tiếng Việt
        context.setVariable("paymentMethod",
                order.getPaymentMethod() != null ? order.getPaymentMethod().getDescription() : "");

        // Thông tin giao hàng - ghép địa chỉ đầy đủ (deliveryAddress + ward + district)
        context.setVariable("receiverName",
                order.getReceiverName() != null ? order.getReceiverName() : "");
        context.setVariable("receiverPhone",
                order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
        context.setVariable("receiverEmail",
                order.getReceiverEmail() != null ? order.getReceiverEmail() : "");
        context.setVariable("deliveryAddress", buildFullAddress(order));
        context.setVariable("deliveryType",
                order.getDeliveryType() != null ? order.getDeliveryType().getDescription() : "");

        return context;
    }

    /**
     * Ghép địa chỉ đầy đủ từ deliveryAddress + ward + district
     * Ví dụ: "123 Nguyễn Văn A, Phường Bến Nghé, Quận 1"
     */
    private String buildFullAddress(Order order) {
        StringBuilder fullAddress = new StringBuilder();

        if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isEmpty()) {
            fullAddress.append(order.getDeliveryAddress());
        }

        // Truy vấn Ward name từ ward_id
        if (order.getWardId() != null) {
            try {
                wardRepository.findById(order.getWardId()).ifPresent(ward -> {
                    if (ward.getName() != null && !ward.getName().isEmpty()) {
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(ward.getName());
                    }
                });
            } catch (Exception e) {
                log.warn("Không thể truy vấn ward_id={}: {}", order.getWardId(), e.getMessage());
            }
        }

        // Truy vấn District name từ district_id
        if (order.getDistrictId() != null) {
            try {
                districtRepository.findById(order.getDistrictId()).ifPresent(district -> {
                    if (district.getName() != null && !district.getName().isEmpty()) {
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(district.getName());
                    }
                });
            } catch (Exception e) {
                log.warn("Không thể truy vấn district_id={}: {}", order.getDistrictId(), e.getMessage());
            }
        }

        return fullAddress.toString();
    }

    /**
     * Tạo thông báo cập nhật trạng thái đơn hàng cho User
     */
    private void createOrderStatusNotificationForUser(Order updatedOrder, String oldStatus) {
        try {
            String newStatus = updatedOrder.getStatus().toString();
            String title = getOrderStatusTitleForUser(newStatus);
            String message = getOrderStatusMessageForUser(updatedOrder.getOrderCode(), newStatus, oldStatus);
            String notificationType = getNotificationTypeForStatus(newStatus);

            // Sử dụng NotificationHelper để tạo thông báo cho user
            notificationHelper.createOrderStatusNotificationForUser(
                updatedOrder.getUserId(),
                updatedOrder.getId(),
                updatedOrder.getOrderCode(),
                title,
                message,
                notificationType
            );

            log.info("Đã tạo thông báo cập nhật trạng thái cho user {} về đơn hàng {}: {} -> {}",
                    updatedOrder.getUserId(), updatedOrder.getOrderCode(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo cho user {} về đơn hàng {}: {}",
                    updatedOrder.getUserId(), updatedOrder.getOrderCode(), e.getMessage());
        }
    }

    /**
     * Lấy tiêu đề thông báo cho User dựa trên trạng thái đơn hàng
     */
    private String getOrderStatusTitleForUser(String status) {
        switch (status) {
            case "CONFIRMED":
                return "Đơn hàng đã được xác nhận";
            case "DELIVERING":
                return "Đơn hàng đang được giao";
            case "COMPLETED":
                return "Đơn hàng đã được giao thành công";
            case "CANCELLED":
                return "Đơn hàng đã bị hủy";
            default:
                return "Cập nhật trạng thái đơn hàng";
        }
    }

    /**
     * Lấy nội dung thông báo cho User dựa trên trạng thái đơn hàng
     */
    private String getOrderStatusMessageForUser(String orderCode, String newStatus, String oldStatus) {
        switch (newStatus) {
            case "CONFIRMED":
                return String.format("Đơn hàng %s của bạn đã được xác nhận và đang được chuẩn bị.", orderCode);
            case "DELIVERING":
                return String.format("Đơn hàng %s đã chuẩn bị xong. Vui lòng chuẩn bị nhận hàng.", orderCode);
            case "COMPLETED":
                return String.format("Đơn hàng %s đã được giao thành công. Cảm ơn bạn đã sử dụng dịch vụ!", orderCode);
            case "CANCELLED":
                return String.format("Đơn hàng %s đã bị hủy. Xin lỗi vì sự bất tiện này.", orderCode);
            default:
                return String.format("Trạng thái đơn hàng %s đã được cập nhật từ %s sang %s.", orderCode, oldStatus, newStatus);
        }
    }

    /**
     * Lấy loại thông báo dựa trên trạng thái đơn hàng
     */
    private String getNotificationTypeForStatus(String status) {
        switch (status) {
            case "CONFIRMED":
                return "ORDER_CONFIRMED";
            case "DELIVERING":
                return "ORDER_DELIVERING";
            case "COMPLETED":
                return "ORDER_COMPLETED";
            case "CANCELLED":
                return "ORDER_CANCELLED";
            default:
                return "ORDER_STATUS_UPDATE";
        }
    }
}
