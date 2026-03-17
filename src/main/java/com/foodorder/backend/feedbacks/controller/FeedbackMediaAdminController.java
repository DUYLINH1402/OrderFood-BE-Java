package com.foodorder.backend.feedbacks.controller;

import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;
import com.foodorder.backend.feedbacks.service.FeedbackMediaService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller admin cho feedback media - CRUD (Create/Update/Delete)
 * Yêu cầu quyền ADMIN
 *
 * Đã migrate từ POST/PUT/DELETE /api/feedback-media → /api/v1/admin/feedback-media (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/admin/feedback-media")
@RequiredArgsConstructor
@RequireAdmin
@Tag(name = "Feedback Media - Admin", description = "API quản lý nội dung phản hồi - Yêu cầu quyền Admin")
public class FeedbackMediaAdminController {

    private final FeedbackMediaService service;

    @Operation(summary = "Tạo feedback media", description = "Tạo mới nội dung phản hồi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @PostMapping
    public FeedbackMediaResponse create(@Valid @RequestBody FeedbackMediaRequest req) {
        return service.create(req);
    }

    @Operation(summary = "Cập nhật feedback media", description = "Cập nhật nội dung phản hồi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @PutMapping("/{id}")
    public FeedbackMediaResponse update(
            @Parameter(description = "ID của feedback media") @PathVariable Long id,
            @Valid @RequestBody FeedbackMediaRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Xóa feedback media", description = "Xóa nội dung phản hồi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID của feedback media") @PathVariable Long id) {
        service.delete(id);
    }
}

