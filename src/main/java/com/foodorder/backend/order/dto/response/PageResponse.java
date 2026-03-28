package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response wrapper for paginated data")
public class PageResponse<T> {

    @Schema(description = "List of data for current page")
    private List<T> data;

    @Schema(description = "Current page number (starts from 0)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "10")
    private int size;

    @Schema(description = "Total number of items", example = "100")
    private long total;

    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;

    @Schema(description = "Has next page or not", example = "true")
    private boolean hasNext;

    @Schema(description = "Has previous page or not", example = "false")
    private boolean hasPrevious;
}
