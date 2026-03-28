package com.foodorder.backend.comment.controller;

import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.service.CommentService;
import com.foodorder.backend.like.entity.TargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API bình luận công khai (không cần đăng nhập)
 * Xem danh sách bình luận, chi tiết, reply, đếm số lượng
 */
@RestController
@RequestMapping("/api/v1/public/comments")
@RequiredArgsConstructor
@Tag(name = "Comments - Public", description = "Public APIs for viewing comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{targetType}/{targetId}")
    @Operation(summary = "Get comments", description = "Retrieve comments for a target (FOOD, BLOG...)")
    public ResponseEntity<CommentPageResponse> getCommentsByTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByTarget(type, targetId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{commentId}")
    @Operation(summary = "Get comment detail", description = "Retrieve detailed information of a comment")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Get replies", description = "Retrieve replies for a comment")
    public ResponseEntity<CommentPageResponse> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getReplies(commentId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/{targetType}/{targetId}")
    @Operation(summary = "Count comments", description = "Count comments for a target")
    public ResponseEntity<Long> countComments(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        long count = commentService.countCommentsByTarget(type, targetId);
        return ResponseEntity.ok(count);
    }
}
