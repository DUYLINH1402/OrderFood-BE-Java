package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc cập nhật ghi chú nội bộ (internal_note)
 * Dùng cho đối soát, lưu ý về dòng tiền hoặc khách hàng mà chỉ nội bộ quản trị thấy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating internal note of order")
public class UpdateInternalNoteRequest {

    @Schema(
        description = "Internal note (Admin/Staff only, for reconciliation, notes about payments or customers)",
        example = "VIP customer, prioritize delivery"
    )
    @Size(max = 2000, message = "Internal note cannot exceed 2000 characters")
    private String internalNote;
}

