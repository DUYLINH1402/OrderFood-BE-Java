package com.foodorder.backend.like;

import com.foodorder.backend.like.dto.request.LikeRequest;
import com.foodorder.backend.like.dto.response.LikeResponse;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.like.service.LikeService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller client cho Like - Toggle like và kiểm tra trạng thái
 * Yêu cầu đăng nhập
 *
 * Đã migrate từ POST /api/likes/toggle, GET /api/likes/check → /api/v1/client/likes (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/client/likes")
@RequiredArgsConstructor
@Tag(name = "Like - Client", description = "Like operations API - Requires authentication")
public class LikeClientController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    @Operation(summary = "Toggle like/unlike", description = "Like if not liked yet, unlike if already liked")
    public ResponseEntity<LikeResponse> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LikeRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        LikeResponse response = likeService.toggleLike(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{targetType}/{targetId}")
    @Operation(summary = "Check if liked", description = "Check if current user has liked the target")
    public ResponseEntity<Boolean> checkLiked(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String targetType,
            @PathVariable Long targetId
    ) {
        Long userId = userDetails.getUser().getId();
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        boolean isLiked = likeService.isLiked(userId, type, targetId);
        return ResponseEntity.ok(isLiked);
    }
}

