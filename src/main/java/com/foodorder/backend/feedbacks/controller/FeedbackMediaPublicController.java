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
@Tag(name = "Feedback Media - Public", description = "API xem nội dung phản hồi - Công khai")
public class FeedbackMediaPublicController {

    private final FeedbackMediaService service;

    @Operation(summary = "Lấy tất cả feedback media", description = "Lấy danh sách tất cả nội dung phản hồi.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public FeedbackMediaListResponse getAll() {
        return service.getAll();
    }

    @Operation(summary = "Chi tiết feedback media", description = "Lấy thông tin chi tiết một nội dung phản hồi theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @GetMapping("/{id}")
    public FeedbackMediaResponse getById(
            @Parameter(description = "ID của feedback media") @PathVariable Long id) {
        return service.getById(id);
    }
}

