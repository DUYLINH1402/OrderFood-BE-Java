package com.foodorder.backend.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Response wrapper cho kết quả tìm kiếm từ Algolia
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing food search results from Algolia")
public class FoodSearchResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "List of food items found")
    private List<FoodSearchDTO> results;

    @Schema(description = "Search keyword", example = "pho bo")
    private String query;

    @Schema(description = "Total number of results", example = "15")
    private int totalResults;

    @Schema(description = "Current page number (starting from 0)", example = "0")
    private int page;

    @Schema(description = "Number of results per page", example = "10")
    private int hitsPerPage;
}
