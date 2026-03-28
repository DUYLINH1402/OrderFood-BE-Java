package com.foodorder.backend.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO request để cập nhật bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update a comment")
public class UpdateCommentRequest {

    @NotBlank(message = "COMMENT_CONTENT_REQUIRED")
    @Size(min = 1, max = 2000, message = "COMMENT_CONTENT_LENGTH_1_2000")
    @Schema(description = "New comment content", example = "This dish is delicious, will come back!")
    private String content;
}
