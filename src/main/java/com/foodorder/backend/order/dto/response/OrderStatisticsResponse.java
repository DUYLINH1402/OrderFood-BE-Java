package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing order statistics")
public class OrderStatisticsResponse {

    @Schema(description = "Total number of orders", example = "500")
    private long totalOrders;

    @Schema(description = "Number of completed orders", example = "450")
    private long completedOrders;

    @Schema(description = "Number of cancelled orders", example = "15")
    private long cancelledOrders;

    @Schema(description = "Number of pending orders", example = "20")
    private long pendingOrders;

    @Schema(description = "Total revenue (VND)", example = "15000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Average order value (VND)", example = "85000")
    private BigDecimal averageOrderValue;

    // Old fields for backward compatibility (optional)
    @Schema(description = "Number of confirmed orders (deprecated)", example = "10")
    private long confirmedOrders;

    @Schema(description = "Number of orders being prepared (deprecated)", example = "5")
    private long preparingOrders;

    @Schema(description = "Number of orders being delivered (deprecated)", example = "8")
    private long shippingOrders;

    @Schema(description = "Number of delivered orders (deprecated)", example = "450")
    private long deliveredOrders;

    @Schema(description = "Total spent (deprecated)", example = "15000000")
    private BigDecimal totalSpent;
}
