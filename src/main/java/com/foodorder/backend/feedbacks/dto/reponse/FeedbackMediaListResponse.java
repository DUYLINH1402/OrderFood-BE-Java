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
@Schema(description = "Response containing list of feedback media")
public class FeedbackMediaListResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "List of feedback media")
    private List<FeedbackMediaResponse> feedbacks;

    @Schema(description = "Total number of feedback media", example = "10")
    private Integer total;

    public static FeedbackMediaListResponse of(List<FeedbackMediaResponse> items) {
        return FeedbackMediaListResponse.builder()
                .feedbacks(items)
                .total(items.size())
                .build();
    }
}

