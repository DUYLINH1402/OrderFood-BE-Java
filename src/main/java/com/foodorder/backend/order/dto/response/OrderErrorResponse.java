package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing error information when processing order")
public class OrderErrorResponse {

    @Schema(description = "Success or failure status", example = "false")
    private boolean success;

    @Schema(description = "Detailed error message", example = "Insufficient points to use")
    private String message;

    @Schema(description = "Standardized error code", example = "INSUFFICIENT_POINTS")
    private String errorCode;

    @Schema(description = "Error timestamp (Unix milliseconds)", example = "1705744800000")
    private long timestamp;

    // Common error codes for Order
    public static final String INSUFFICIENT_POINTS = "INSUFFICIENT_POINTS";
    public static final String POINTS_EXCEED_ORDER_VALUE = "POINTS_EXCEED_ORDER_VALUE";
    public static final String POINTS_GUEST_ORDER = "POINTS_GUEST_ORDER";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String COUPON_INVALID = "COUPON_INVALID";
    public static final String FOOD_NOT_FOUND = "FOOD_NOT_FOUND";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
