package com.foodorder.backend.comment.dto.request;

import com.foodorder.backend.comment.entity.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO request to update the status of multiple comments at once
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to batch update comment status")
public class BatchUpdateStatusRequest {

    @NotEmpty(message = "COMMENT_IDS_REQUIRED")
    @Schema(description = "List of comment IDs to update", example = "[1, 2, 3]")
    private List<Long> commentIds;

    @NotNull(message = "STATUS_REQUIRED")
    @Schema(description = "New status (ACTIVE, HIDDEN, DELETED)", example = "HIDDEN")
    private CommentStatus status;
}
