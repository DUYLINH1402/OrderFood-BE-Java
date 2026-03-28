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
@Tag(name = "Comments - Client", description = "Comment APIs for authenticated users")
public class CommentClientController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create comment", description = "Create a comment on food, blog, etc. Can be a reply if parentId is provided")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = userDetails.getUser().getId();
        CommentResponse response = commentService.createComment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update comment", description = "Only the comment owner can update their comment")
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
    @Operation(summary = "Delete comment", description = "Delete own comment (soft delete)")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId) {
        Long userId = userDetails.getUser().getId();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my comments", description = "Retrieve comments posted by the current user")
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
