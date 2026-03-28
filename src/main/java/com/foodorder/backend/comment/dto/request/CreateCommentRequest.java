package com.foodorder.backend.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO request để tạo bình luận mới
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new comment")
public class CreateCommentRequest {

    @NotBlank(message = "COMMENT_CONTENT_REQUIRED")
    @Size(min = 1, max = 2000, message = "COMMENT_CONTENT_LENGTH_1_2000")
    @Schema(description = "Comment content", example = "This dish is delicious!")
    private String content;

    @NotNull(message = "TARGET_TYPE_REQUIRED")
    @Schema(description = "Target type (FOOD, BLOG...)", example = "FOOD", allowableValues = {"FOOD", "BLOG", "MOVIE"})
    private String targetType;

    @NotNull(message = "TARGET_ID_REQUIRED")
    @Schema(description = "Target ID being commented on", example = "1")
    private Long targetId;

    @Schema(description = "Parent comment ID (if this is a reply)", example = "null")
    private Long parentId;
}
