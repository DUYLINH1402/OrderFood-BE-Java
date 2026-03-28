package com.foodorder.backend.feedbacks.dto.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;


@Getter
@Setter
@Schema(description = "Response containing feedback media information")
public class FeedbackMediaResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Media ID", example = "1")
    private Long id;

    @Schema(description = "Media type", example = "IMAGE", allowableValues = {"IMAGE", "VIDEO"})
    private String type;

    @Schema(description = "Media URL", example = "https://example.com/feedback/image1.jpg")
    private String mediaUrl;

    @Schema(description = "Thumbnail URL", example = "https://example.com/feedback/thumb1.jpg")
    private String thumbnailUrl;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Creation time", example = "2025-01-20 10:30:00")
    private Timestamp createdAt;
}
