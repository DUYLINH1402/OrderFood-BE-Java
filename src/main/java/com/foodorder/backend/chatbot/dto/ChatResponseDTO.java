package com.foodorder.backend.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response từ chatbot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing AI chatbot reply")
public class ChatResponseDTO {

    @Schema(description = "Conversation session ID", example = "session_abc123")
    private String sessionId;

    @Schema(description = "Bot response message", example = "Hello! How can I help you?")
    private String message;

    @Schema(description = "Message type", example = "text", allowableValues = {"text", "suggestion", "product_recommendation"})
    private String messageType;

    @Schema(description = "Response timestamp", example = "2025-01-20T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Response processing time (ms)", example = "150")
    private Integer responseTime;

    @Schema(description = "Suggested follow-up questions")
    private List<String> suggestions;

    @Schema(description = "Quick action buttons")
    private List<QuickActionDTO> quickActions;

    @Schema(description = "Product recommendation data")
    private RecommendationDataDTO recommendationData;

    @Schema(description = "Whether response is from knowledge base", example = "true")
    private Boolean isFromKnowledgeBase;

    @Schema(description = "Answer confidence score (0-1)", example = "0.95")
    private Double confidenceScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Quick action for the user")
    public static class QuickActionDTO {
        @Schema(description = "Display label", example = "View Menu")
        private String label;

        @Schema(description = "Action type", example = "view_menu", allowableValues = {"view_menu", "place_order", "track_order", "contact_support"})
        private String action;

        @Schema(description = "Link URL (if applicable)", example = "/menu")
        private String url;

        @Schema(description = "Additional action data")
        private Object data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Product recommendation data")
    public static class RecommendationDataDTO {
        @Schema(description = "Recommended food items")
        private List<ProductRecommendationDTO> foods;

        @Schema(description = "Recommendation reason", example = "Based on your preferences")
        private String reason;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Recommended food item details")
        public static class ProductRecommendationDTO {
            @Schema(description = "Food item ID", example = "1")
            private Long id;

            @Schema(description = "Food item name", example = "Pho Bo Tai")
            private String name;

            @Schema(description = "Food description", example = "Beef pho with rich broth")
            private String description;

            @Schema(description = "Food price", example = "55000")
            private Double price;

            @Schema(description = "Image URL", example = "https://example.com/pho.jpg")
            private String imageUrl;

            @Schema(description = "Food category", example = "Main Dishes")
            private String category;

            @Schema(description = "Average rating", example = "4.5")
            private Double rating;
        }
    }
}
