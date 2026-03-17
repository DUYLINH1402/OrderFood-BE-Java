package com.foodorder.backend.feedbacks.dto.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Wrapper DTO cho danh sách FeedbackMediaResponse
 * Cần thiết để Redis cache serialize/deserialize đúng kiểu (tránh lỗi Jackson type info với raw List)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa danh sách feedback media")
public class FeedbackMediaListResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Danh sách feedback media")
    private List<FeedbackMediaResponse> items;

    @Schema(description = "Tổng số lượng feedback media", example = "10")
    private int total;

    public static FeedbackMediaListResponse of(List<FeedbackMediaResponse> items) {
        return FeedbackMediaListResponse.builder()
                .items(items)
                .total(items.size())
                .build();
    }
}

