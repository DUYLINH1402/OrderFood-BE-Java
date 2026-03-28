package com.foodorder.backend.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Request body để cập nhật trạng thái bảo vệ (isProtected) của dữ liệu
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update the protected status of data")
public class ProtectedStatusRequest {

    @Schema(description = "New protection status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isProtected;
}

