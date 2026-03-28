package com.foodorder.backend.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO Request để thêm/cập nhật hình ảnh gallery
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add/update a gallery image")
public class GalleryRequest {

    @NotBlank(message = "Image URL must not be blank")
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Schema(description = "Image URL", example = "https://example.com/image.jpg", required = true)
    private String imageUrl;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;
}
