package com.foodorder.backend.chatbot.dto;

import com.foodorder.backend.chatbot.entity.KnowledgeBase.KnowledgeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * DTO cho việc tạo và cập nhật Knowledge Base
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating/updating Knowledge Base for chatbot")
public class KnowledgeBaseDTO {

    @Schema(description = "Knowledge base ID", example = "1")
    private Long id;

    @Schema(
        description = "Knowledge base title",
        example = "How to place an order",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "TITLE_REQUIRED")
    private String title;

    @Schema(
        description = "Detailed knowledge base content",
        example = "To place an order, select food items, add to cart and proceed to checkout...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "CONTENT_REQUIRED")
    private String content;

    @Schema(
        description = "Related keywords (comma-separated)",
        example = "order, purchase, checkout, payment"
    )
    private String keywords;

    @Schema(
        description = "Knowledge base category",
        example = "FAQ",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "CATEGORY_REQUIRED")
    private KnowledgeCategory category;

    @Schema(
        description = "Display priority (1-10, lower number = higher priority)",
        example = "1",
        minimum = "1",
        maximum = "10"
    )
    @Min(value = 1, message = "PRIORITY_MIN_1")
    @Max(value = 10, message = "PRIORITY_MAX_10")
    private Integer priority;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Created timestamp", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp", example = "2025-01-20T15:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Creator user ID", example = "1")
    private Long createdBy;
}
