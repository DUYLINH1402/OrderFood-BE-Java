package com.foodorder.backend.feedbacks.controller;

import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaListResponse;
import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.service.FeedbackMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller public cho feedback media - Xem danh sách và chi tiết
 * Không yêu cầu đăng nhập
 *
 * Đã migrate từ GET /api/feedback-media → /api/v1/public/feedback-media (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/feedback-media")
@RequiredArgsConstructor
@Tag(name = "Feedback Media - Public", description = "Public API for feedback content")
public class FeedbackMediaPublicController {

    private final FeedbackMediaService service;

    @Operation(summary = "Get all feedback media", description = "Get list of all feedback content.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public FeedbackMediaListResponse getAll() {
        return service.getAll();
    }

    @Operation(summary = "Get feedback media details", description = "Get detailed information of a feedback content by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public FeedbackMediaResponse getById(
            @Parameter(description = "Feedback media ID") @PathVariable Long id) {
        return service.getById(id);
    }
}

