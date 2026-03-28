package com.foodorder.backend.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DTO Response trả về thông tin đầy đủ của nhà hàng kèm danh sách ảnh gallery
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing detailed restaurant information")
public class RestaurantResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Restaurant ID", example = "1")
    private Long id;

    @Schema(description = "Restaurant name", example = "Dong Xanh Restaurant")
    private String name;

    @Schema(description = "Restaurant logo URL", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "Restaurant address", example = "123 Nguyen Van Linh, District 7, Ho Chi Minh City")
    private String address;

    @Schema(description = "Contact phone number", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "Introduction video URL", example = "https://youtube.com/watch?v=xxx")
    private String videoUrl;

    @Schema(description = "Detailed description of the restaurant")
    private String description;

    @Schema(description = "Opening hours", example = "07:00 - 22:00")
    private String openingHours;

    @Schema(description = "List of restaurant gallery images")
    private List<GalleryItemDTO> galleries;

    /**
     * DTO cho từng item trong gallery
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Information of a single gallery image")
    public static class GalleryItemDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Gallery image ID", example = "1")
        private Long id;

        @Schema(description = "Image URL", example = "https://example.com/image1.jpg")
        private String imageUrl;

        @Schema(description = "Display order", example = "1")
        private Integer displayOrder;
    }
}
