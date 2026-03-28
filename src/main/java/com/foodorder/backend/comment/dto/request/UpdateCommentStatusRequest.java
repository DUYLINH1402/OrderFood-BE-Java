package com.foodorder.backend.comment.dto.request;

import com.foodorder.backend.comment.entity.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO request để admin thay đổi trạng thái bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update comment status (Admin only)")
public class UpdateCommentStatusRequest {

    @NotNull(message = "STATUS_REQUIRED")
    @Schema(description = "New comment status", example = "HIDDEN", allowableValues = {"ACTIVE", "HIDDEN", "DELETED"})
    private CommentStatus status;
}
