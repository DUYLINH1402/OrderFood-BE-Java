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
@Tag(name = "Feedback Media - Admin", description = "Feedback content management API - Requires Admin access")
public class FeedbackMediaAdminController {

    private final FeedbackMediaService service;

    @Operation(summary = "Create feedback media", description = "Create new feedback content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created successfully"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PostMapping
    public FeedbackMediaResponse create(@Valid @RequestBody FeedbackMediaRequest req) {
        return service.create(req);
    }

    @Operation(summary = "Update feedback media", description = "Update feedback content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public FeedbackMediaResponse update(
            @Parameter(description = "Feedback media ID") @PathVariable Long id,
            @Valid @RequestBody FeedbackMediaRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Delete feedback media", description = "Delete feedback content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "Feedback media ID") @PathVariable Long id) {
        service.delete(id);
    }
}

