package com.foodorder.backend.like;

import com.foodorder.backend.like.dto.response.LikeResponse;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.like.service.LikeService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller public cho Like - Xem thông tin like
 * Không bắt buộc đăng nhập, nhưng nếu đã đăng nhập sẽ trả về trạng thái "đã like" của user
 *
 * Đã migrate từ GET /api/likes/{targetType}/{targetId} → /api/v1/public/likes (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/public/likes")
@RequiredArgsConstructor
@Tag(name = "Like - Public", description = "API xem thông tin lượt thích - Công khai")
public class LikePublicController {

    private final LikeService likeService;

    @GetMapping("/{targetType}/{targetId}")
    @Operation(summary = "Lấy thông tin like", description = "Lấy số lượt like và trạng thái đã like của user (nếu đã đăng nhập)")
    public ResponseEntity<LikeResponse> getLikeInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String targetType,
            @PathVariable Long targetId
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        LikeResponse response = likeService.getLikeInfo(userId, type, targetId);
        return ResponseEntity.ok(response);
    }
}

