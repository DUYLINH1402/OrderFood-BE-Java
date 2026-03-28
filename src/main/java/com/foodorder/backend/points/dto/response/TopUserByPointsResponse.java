package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO chứa thông tin xếp hạng user theo điểm thưởng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing user ranking by reward points")
public class TopUserByPointsResponse {

    @Schema(description = "Rank position", example = "1")
    private Integer rank;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Current points balance", example = "500")
    private Integer currentBalance;

    @Schema(description = "Total points earned", example = "2000")
    private Long totalPointsEarned;

    @Schema(description = "Total points used", example = "1500")
    private Long totalPointsUsed;

    @Schema(description = "Total number of orders", example = "50")
    private Long totalOrders;
}
