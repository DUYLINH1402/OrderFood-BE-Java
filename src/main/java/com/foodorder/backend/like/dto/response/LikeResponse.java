package com.foodorder.backend.like.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response for like/unlike result")
public class LikeResponse {

    @Schema(description = "Current like status (true = liked, false = not liked)", example = "true")
    private boolean liked;

    @Schema(description = "Total like count of target", example = "150")
    private long totalLikes;

    @Schema(description = "Target type", example = "FOOD")
    private String targetType;

    @Schema(description = "Target ID", example = "1")
    private Long targetId;
}

