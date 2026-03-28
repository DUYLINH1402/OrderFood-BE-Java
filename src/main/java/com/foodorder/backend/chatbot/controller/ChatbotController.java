package com.foodorder.backend.chatbot.controller;

import com.foodorder.backend.chatbot.dto.ChatRequestDTO;
import com.foodorder.backend.chatbot.entity.ChatbotMessage;
import com.foodorder.backend.chatbot.service.ChatbotService;
import com.foodorder.backend.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API cho Chatbot AI
 */
@RestController
@RequestMapping("/api/v1/public/chatbot")
@RequiredArgsConstructor

@Slf4j
@Tag(name = "Chatbot", description = "AI Chatbot APIs for customer support")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(summary = "Chat with bot", description = "Send a message and receive a response from the AI chatbot.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/chat")
    public Mono<ResponseEntity<Object>> chat(
            @Valid @RequestBody ChatRequestDTO request,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            // Lấy user ID nếu đã đăng nhập
            if (authentication != null && request.getUserId() == null) {
                log.debug("User đã đăng nhập nhưng chưa set userId trong request");
            }

            log.info("Received chat message: {}", request.getMessage());

            return chatbotService.processMessage(request)
                .map(response -> ResponseEntity.ok().body((Object) response))
                .onErrorResume(error -> {
                    log.error("Error in chat API: {}", error.getMessage(), error);
                    ApiError apiError = ApiError.builder()
                        .errorCode("CHATBOT_ERROR")
                        .message("Failed to process chat message")
                        .details(error.getMessage())
                        .build();
                    return Mono.just(ResponseEntity.internalServerError().body((Object) apiError));
                });

        } catch (Exception e) {
            log.error("Validation error in chat API: {}", e.getMessage(), e);
            ApiError apiError = ApiError.builder()
                .errorCode("INVALID_REQUEST")
                .message("Invalid request")
                .details(e.getMessage())
                .build();
            return Mono.just(ResponseEntity.badRequest().body((Object) apiError));
        }
    }

    @Operation(summary = "Chat history", description = "Retrieve chat history by session ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid session ID")
    })
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getChatHistory(
            @Parameter(description = "Conversation session ID") @PathVariable String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                ApiError apiError = ApiError.builder()
                    .errorCode("INVALID_SESSION_ID")
                    .message("Invalid session ID")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

            List<ChatbotMessage> history = chatbotService.getChatHistory(sessionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", history
            ));

        } catch (Exception e) {
            log.error("Error fetching chat history: {}", e.getMessage(), e);
            ApiError apiError = ApiError.builder()
                .errorCode("HISTORY_FETCH_ERROR")
                .message("Failed to fetch chat history")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    @Operation(summary = "Rate response", description = "Rate chatbot response quality (1-5 stars).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating submitted"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping("/rate")
    public ResponseEntity<?> rateResponse(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            Object messageIdObj = request.get("messageId");
            Object ratingObj = request.get("rating");

            if (sessionId == null || messageIdObj == null || ratingObj == null) {
                ApiError apiError = ApiError.builder()
                    .errorCode("MISSING_PARAMETERS")
                    .message("Missing required parameters")
                    .details("sessionId, messageId and rating are required")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

            Long messageId = Long.valueOf(messageIdObj.toString());
            Integer rating = Integer.valueOf(ratingObj.toString());

            if (rating < 1 || rating > 5) {
                ApiError apiError = ApiError.builder()
                    .errorCode("INVALID_RATING")
                    .message("Rating must be between 1 and 5")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

            boolean success = chatbotService.rateResponse(sessionId, messageId, rating);

            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rating submitted successfully"
                ));
            } else {
                ApiError apiError = ApiError.builder()
                    .errorCode("RATING_FAILED")
                    .message("Unable to rate response")
                    .details("Message does not exist or does not belong to this session")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

        } catch (NumberFormatException e) {
            log.error("Number format error in rate response: {}", e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("INVALID_NUMBER_FORMAT")
                .message("Invalid number format")
                .details("messageId and rating must be valid numbers")
                .build();
            return ResponseEntity.badRequest().body(apiError);
        } catch (Exception e) {
            log.error("Error rating response: {}", e.getMessage(), e);
            ApiError apiError = ApiError.builder()
                .errorCode("RATING_ERROR")
                .message("Failed to rate response")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    @Operation(summary = "Health check", description = "Check chatbot service status.")
    @ApiResponse(responseCode = "200", description = "Service is running")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "chatbot",
            "timestamp", System.currentTimeMillis(),
            "version", "1.0.0"
        ));
    }

}
