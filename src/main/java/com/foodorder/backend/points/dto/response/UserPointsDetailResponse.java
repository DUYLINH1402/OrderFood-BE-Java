package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa thông tin điểm thưởng của một User (dành cho Admin)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing detailed reward points information of a user (Admin only)")
public class UserPointsDetailResponse {

    // === THÔNG TIN USER ===
    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    // === SỐ DƯ HIỆN TẠI ===
    @Schema(description = "Current points balance", example = "500")
    private Integer currentBalance;

    @Schema(description = "Last updated time", example = "2025-01-20T10:30:00")
    private LocalDateTime lastUpdated;

    // === THỐNG KÊ TỔNG QUAN ===
    @Schema(description = "Total points earned", example = "2000")
    private Long totalPointsEarned;

    @Schema(description = "Total points used", example = "1500")
    private Long totalPointsUsed;

    @Schema(description = "Total points refunded", example = "100")
    private Long totalPointsRefunded;

    @Schema(description = "Total number of point transactions", example = "75")
    private Long totalTransactions;

    // === LỊCH SỬ GẦN ĐÂY ===
    @Schema(description = "Recent point transactions")
    private List<PointTransactionDetail> recentTransactions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Detail of a single point transaction")
    public static class PointTransactionDetail {
        @Schema(description = "Transaction ID", example = "1")
        private Long transactionId;

        @Schema(description = "Transaction type", example = "EARN", allowableValues = {"EARN", "USE", "REFUND", "EXPIRE"})
        private String type;

        @Schema(description = "Points amount", example = "100")
        private Integer amount;

        @Schema(description = "Related order ID", example = "50")
        private Long orderId;

        @Schema(description = "Transaction description", example = "Points earned from order")
        private String description;

        @Schema(description = "Transaction time", example = "2025-01-20T10:30:00")
        private LocalDateTime createdAt;
    }
}
