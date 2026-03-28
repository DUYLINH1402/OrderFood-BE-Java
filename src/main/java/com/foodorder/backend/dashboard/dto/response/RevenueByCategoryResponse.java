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
 * Response DTO cho API cơ cấu doanh thu theo nhóm món
 * Hiển thị 3 nhóm chính và 1 nhóm "Khác" cho các danh mục còn lại
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing revenue breakdown by food category")
public class RevenueByCategoryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "List of revenue breakdown (top 3 categories + 'Others')")
    private List<CategoryRevenue> categories;

    @Schema(description = "Total revenue of all categories (VND)", example = "15000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Statistics period (7, 30, 90 days)", example = "30")
    private Integer periodDays;

    /**
     * DTO cho doanh thu từng nhóm món
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Revenue by category")
    public static class CategoryRevenue implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "Category ID (null if 'Others' group)", example = "1")
        private Long categoryId;

        @Schema(description = "Category name", example = "Main dishes")
        private String categoryName;

        @Schema(description = "Category slug", example = "main-dishes")
        private String categorySlug;

        @Schema(description = "Category revenue (VND)", example = "5000000")
        private BigDecimal revenue;

        @Schema(description = "Revenue percentage of total", example = "33.3")
        private Double percentage;

        @Schema(description = "Number of orders containing items from this category", example = "100")
        private Long orderCount;

        @Schema(description = "Quantity sold from this category", example = "200")
        private Long quantitySold;

        @Schema(description = "Color for chart display", example = "#FF6384")
        private String color;
    }
}
