package com.foodorder.backend.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * DTO request để xóa nhiều bình luận cùng lúc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to batch delete comments")
public class BatchDeleteRequest {

    @NotEmpty(message = "COMMENT_IDS_REQUIRED")
    @Schema(description = "List of comment IDs to delete", example = "[1, 2, 3]")
    private List<Long> commentIds;
}
