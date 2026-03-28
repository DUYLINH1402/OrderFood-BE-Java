package com.foodorder.backend.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO Request để cập nhật thông tin nhà hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update restaurant information")
public class RestaurantUpdateRequest {

    @NotBlank(message = "Restaurant name must not be blank")
    @Size(max = 255, message = "Restaurant name must not exceed 255 characters")
    @Schema(description = "Restaurant name", example = "Dong Xanh Restaurant", required = true)
    private String name;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    @Schema(description = "Restaurant logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Restaurant address", example = "123 Nguyen Van Linh, District 7, Ho Chi Minh City")
    private String address;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Contact phone number", example = "0901234567")
    private String phoneNumber;

    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    @Schema(description = "Introduction video URL", example = "https://youtube.com/watch?v=xxx")
    private String videoUrl;

    @Schema(description = "Detailed description of the restaurant")
    private String description;

    @Size(max = 100, message = "Opening hours must not exceed 100 characters")
    @Schema(description = "Opening hours", example = "07:00 - 22:00")
    private String openingHours;
}
