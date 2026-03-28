package com.foodorder.backend.notifications.controller;

import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.service.NotificationService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các API thông báo cho User (khách hàng)
 * Yêu cầu đăng nhập
 *
 * Đã migrate từ /api/notifications/user → /api/v1/client/notifications (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/client/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications - Client", description = "Notification API for customers - Requires authentication")
public class UserNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Get all notifications for current user (with pagination).")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") int size) {

//        log.info("User {} lấy danh sách thông báo, page: {}, size: {}",
//                userDetails.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByUser(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications", description = "Get list of unread notifications.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} lấy danh sách thông báo chưa đọc", userDetails.getId());

        List<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(unreadNotifications);
    }

    @Operation(summary = "Count unread notifications", description = "Get count of unread notifications.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} lấy số lượng thông báo chưa đọc", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @Operation(summary = "Mark as read", description = "Mark a notification as read.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} đánh dấu thông báo {} đã đọc", userDetails.getId(), id);

        NotificationResponseDTO notification = notificationService
                .markAsReadByUser(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    @Operation(summary = "Mark all as read", description = "Mark all notifications as read.")
    @ApiResponse(responseCode = "200", description = "Success")
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} đánh dấu tất cả thông báo đã đọc", userDetails.getId());

        notificationService.markAllAsReadByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read",
                "status", "success"
        ));
    }

    @Operation(summary = "Delete notification", description = "Delete a user notification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} xóa thông báo ID: {}", userDetails.getId(), id);

        notificationService.deleteNotificationByUser(id, userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Notification deleted successfully",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/user/all
     * Xóa tất cả thông báo của user
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteAllNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "All notifications deleted successfully",
                "status", "success"
        ));
    }
}
