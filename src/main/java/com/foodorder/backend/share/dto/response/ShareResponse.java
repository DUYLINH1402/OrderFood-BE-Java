package com.foodorder.backend.share.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing share action result")
public class ShareResponse {

    @Schema(description = "Total share count for the target object", example = "50")
    private long totalShares;

    @Schema(description = "Type of the target object", example = "FOOD")
    private String targetType;

    @Schema(description = "ID of the target object", example = "1")
    private Long targetId;

    @Schema(description = "Platform used to share", example = "FACEBOOK")
    private String platform;

    @Schema(description = "Result message", example = "Shared successfully")
    private String message;
}

