package com.foodorder.backend.like.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for liking/unliking a target")
public class LikeRequest {

    @NotNull(message = "Target type is required")
    @Schema(description = "Type of target to like", example = "FOOD", allowableValues = {"FOOD", "BLOG", "MOVIE"})
    private String targetType;

    @NotNull(message = "Target ID is required")
    @Schema(description = "ID of target to like", example = "1")
    private Long targetId;
}

