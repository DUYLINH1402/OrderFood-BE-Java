package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về danh sách hoạt động gần đây
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa danh sách hoạt động gần đây")
public class RecentActivityResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Danh sách hoạt động gần đây")
    private List<Activity> activities;

    @Schema(description = "Tổng số hoạt động", example = "10")
    private Integer totalActivities;

    /**
     * DTO con chứa thông tin 1 hoạt động
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Activity information")
    public static class Activity implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Activity type", example = "ORDER", allowableValues = {"ORDER", "USER_REGISTER", "ORDER_COMPLETED", "ORDER_CANCELLED"})
        private String type;

        @Schema(description = "Activity description", example = "New order #ORD-20250120-001")
        private String description;

        @Schema(description = "Timestamp", example = "2025-01-20T10:30:00")
        private LocalDateTime timestamp;

        @Schema(description = "Related ID (orderId or userId)", example = "100")
        private Long referenceId;

        @Schema(description = "Order code (if activity is order-related)", example = "ORD-20250120-001")
        private String orderCode;

        @Schema(description = "Customer name or related user", example = "Nguyen Van A")
        private String customerName;

        @Schema(description = "Amount (if activity is order-related)", example = "150000")
        private BigDecimal amount;

        @Schema(description = "Status (if order-related)", example = "CONFIRMED")
        private String status;
    }
}
