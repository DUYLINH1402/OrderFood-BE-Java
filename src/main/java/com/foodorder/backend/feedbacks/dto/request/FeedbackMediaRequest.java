package com.foodorder.backend.feedbacks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for feedback media (image/video)")
public class FeedbackMediaRequest {

    @Schema(
        description = "Media type",
        example = "IMAGE",
        allowableValues = {"IMAGE", "VIDEO"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String type;

    @Schema(
        description = "Media URL",
        example = "https://example.com/feedback/image1.jpg",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mediaUrl;

    @Schema(
        description = "Thumbnail URL (used for videos)",
        example = "https://example.com/feedback/thumb1.jpg"
    )
    private String thumbnailUrl;

    @Schema(
        description = "Display order",
        example = "1"
    )
    private Integer displayOrder;
}
