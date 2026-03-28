package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho API Top món ăn bán chạy
 * Chứa danh sách 5 món bán chạy nhất theo khoảng thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing top selling foods list")
public class TopSellingFoodResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "List of top selling foods")
    private List<TopFoodItem> topFoods;

    @Schema(description = "Total revenue from top selling foods (VND)", example = "5000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Total quantity sold from top selling foods", example = "500")
    private Long totalQuantitySold;

    @Schema(description = "Statistics period in days (7, 30, 90)", example = "30")
    private Integer periodDays;

    /**
     * DTO cho từng món ăn bán chạy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Top selling food item")
    public static class TopFoodItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Food ID", example = "1")
        private Long foodId;

        @Schema(description = "Food name", example = "Beef Pho")
        private String foodName;

        @Schema(description = "Food slug", example = "beef-pho")
        private String foodSlug;

        @Schema(description = "Representative image URL", example = "https://example.com/pho.jpg")
        private String imageUrl;

        @Schema(description = "Category name", example = "Main dishes")
        private String categoryName;

        @Schema(description = "Quantity sold", example = "150")
        private Long quantitySold;

        @Schema(description = "Revenue from this food (VND)", example = "8250000")
        private BigDecimal revenue;

        @Schema(description = "Percentage compared to top 5 total revenue", example = "25.5")
        private Double revenuePercentage;
    }
}
