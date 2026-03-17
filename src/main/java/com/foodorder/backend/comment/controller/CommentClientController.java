package com.foodorder.backend.comment.controller;

import com.foodorder.backend.comment.dto.request.CreateCommentRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentRequest;
import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.service.CommentService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý API bình luận dành cho User đã đăng nhập
 * Tạo, sửa, xóa bình luận và xem bình luận của mình
 */
@RestController
@RequestMapping("/api/v1/client/comments")
@RequiredArgsConstructor
@Tag(name = "Comments - Client", description = "API bình luận dành cho người dùng đã đăng nhập")
public class CommentClientController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tạo bình luận mới", description = "Tạo bình luận cho món ăn, bài viết... Có thể là reply nếu có parentId")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = userDetails.getUser().getId();
        CommentResponse response = commentService.createComment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật bình luận", description = "Chỉ có thể cập nhật bình luận của chính mình")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        Long userId = userDetails.getUser().getId();
        CommentResponse response = commentService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa bình luận", description = "Xóa bình luận của chính mình (soft delete)")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {
        Long userId = userDetails.getUser().getId();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy bình luận của tôi", description = "Lấy danh sách bình luận của user hiện tại")
    public ResponseEntity<CommentPageResponse> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userDetails.getUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getMyComments(userId, pageable);
        return ResponseEntity.ok(response);
    }
}

