package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing reward points transaction history")
public class PointsHistoryDTO {

    @Schema(description = "Transaction ID", example = "1")
    private Long id;

    @Schema(description = "Transaction type", example = "EARN", allowableValues = {"EARN", "USE", "REFUND", "EXPIRE"})
    private String type;

    @Schema(description = "Points amount (positive = earned, negative = used)", example = "100")
    private Integer amount;

    @Schema(description = "Related order ID (if any)", example = "50")
    private Long orderId;

    @Schema(description = "Transaction description", example = "Points earned from order #ORD-20250120-001")
    private String description;

    @Schema(description = "Transaction time", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Total points after transaction", example = "500")
    private Integer totalPointsAfter;
}
