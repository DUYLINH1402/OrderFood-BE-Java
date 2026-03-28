package com.foodorder.backend.comment.controller;

import com.foodorder.backend.comment.dto.request.BatchDeleteRequest;
import com.foodorder.backend.comment.dto.request.BatchUpdateStatusRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentStatusRequest;
import com.foodorder.backend.comment.dto.response.BatchOperationResponse;
import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.dto.response.CommentStatisticsResponse;
import com.foodorder.backend.comment.entity.CommentStatus;
import com.foodorder.backend.comment.service.CommentService;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling comment management APIs (for Admin)
 */
@RestController
@RequestMapping("/api/v1/admin/comments")
@RequiredArgsConstructor
@RequireAdmin
@Tag(name = "Comments - Admin", description = "Admin APIs for comment management")
public class AdminCommentController {

    private final CommentService commentService;

    /**
     * Retrieve all comments (with pagination)
     */
    @GetMapping
    @Operation(summary = "Get all comments", description = "Retrieve all comments in the system")
    public ResponseEntity<CommentPageResponse> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getAllComments(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve comments by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get comments by status", description = "Filter comments by status (ACTIVE, HIDDEN, DELETED)")
    public ResponseEntity<CommentPageResponse> getCommentsByStatus(
            @PathVariable CommentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Search comments by content
     */
    @GetMapping("/search")
    @Operation(summary = "Search comments", description = "Search comments by content")
    public ResponseEntity<CommentPageResponse> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.searchComments(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update comment status (hide/show/delete)
     */
    @PutMapping("/{commentId}/status")
    @Operation(summary = "Update comment status", description = "Hide, show, or delete a comment")
    public ResponseEntity<CommentResponse> updateCommentStatus(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentStatusRequest request
    ) {
        CommentResponse response = commentService.updateCommentStatus(commentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Permanently delete a comment (hard delete)
     */
    @DeleteMapping("/{commentId}/hard-delete")
    @Operation(summary = "Hard delete comment", description = "Permanently delete a comment from database")
    public ResponseEntity<Void> hardDeleteComment(
            @PathVariable Long commentId
    ) {
        commentService.hardDeleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve comment details
     */
    @GetMapping("/{commentId}")
    @Operation(summary = "Get comment detail", description = "Retrieve comment details (including HIDDEN, DELETED)")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable Long commentId
    ) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all comments by a specific user
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get comments by user", description = "Retrieve all comments by a specific user")
    public ResponseEntity<CommentPageResponse> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve comments for a specific target (food post/article)
     */
    @GetMapping("/target/{targetType}/{targetId}")
    @Operation(summary = "Get comments by target",
               description = "Retrieve comments for a specific target (including HIDDEN, DELETED)")
    public ResponseEntity<CommentPageResponse> getCommentsByTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByTargetForAdmin(type, targetId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve comment statistics overview
     */
    @GetMapping("/statistics")
    @Operation(summary = "Comment statistics", description = "Retrieve comment statistics overview")
    public ResponseEntity<CommentStatisticsResponse> getCommentStatistics() {
        CommentStatisticsResponse response = commentService.getCommentStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Update status for multiple comments at once
     */
    @PutMapping("/batch/status")
    @Operation(summary = "Batch update status", description = "Update status for multiple comments at once")
    public ResponseEntity<BatchOperationResponse> batchUpdateStatus(
            @Valid @RequestBody BatchUpdateStatusRequest request
    ) {
        BatchOperationResponse response = commentService.batchUpdateStatus(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Permanently delete multiple comments from database
     */
    @DeleteMapping("/batch/hard-delete")
    @Operation(summary = "Batch hard delete", description = "Permanently delete multiple comments from database")
    public ResponseEntity<BatchOperationResponse> batchHardDelete(
            @Valid @RequestBody BatchDeleteRequest request
    ) {
        BatchOperationResponse response = commentService.batchHardDelete(request);
        return ResponseEntity.ok(response);
    }
}
