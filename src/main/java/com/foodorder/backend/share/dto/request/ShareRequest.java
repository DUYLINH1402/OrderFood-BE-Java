package com.foodorder.backend.share.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to record a share action")
public class ShareRequest {

    @NotNull(message = "Target type is required")
    @Schema(description = "Type of the shared object", example = "FOOD", allowableValues = {"FOOD", "BLOG"})
    private String targetType;

    @NotNull(message = "Target ID is required")
    @Schema(description = "ID of the shared object", example = "1")
    private Long targetId;

    @NotNull(message = "Platform is required")
    @Schema(description = "Sharing platform", example = "FACEBOOK", allowableValues = {"FACEBOOK", "ZALO"})
    private String platform;
}

