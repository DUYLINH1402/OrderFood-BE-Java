package com.foodorder.backend.notifications.controller;

import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.service.NotificationService;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.security.annotation.RequireStaff;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý các API thông báo cho Staff (nhân viên)
 * Chỉ Staff và Admin mới có thể truy cập
 *
 * Đã migrate từ /api/notifications/staff → /api/v1/staff/notifications (2026-03-17)
 */
@RestController
@RequestMapping("/api/v1/staff/notifications")
@RequiredArgsConstructor
@Slf4j
@RequireStaff
@Tag(name = "Notifications - Staff", description = "Notification API for Staff/Admin")
public class StaffNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Get all notifications for current staff (with pagination and sorting).")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        // Giới hạn size tối đa để tránh tải quá nhiều dữ liệu
        size = Math.min(size, 50);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByStaff(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications", description = "Get list of unread notifications for staff (with pagination).")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

//        log.info("Staff {} lấy danh sách thông báo chưa đọc, page: {}, size: {}",
//                userDetails.getId(), page, size);

        // Giới hạn size tối đa
        size = Math.min(size, 50);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByStaff(userDetails.getId(), pageable);

        return ResponseEntity.ok(unreadNotifications);
    }

    @Operation(summary = "Count unread notifications", description = "Get count of unread notifications.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("Staff {} lấy số lượng thông báo chưa đọc", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByStaff(userDetails.getId());

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

        NotificationResponseDTO notification = notificationService
                .markAsReadByStaff(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /api/notifications/staff/read-all
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsReadByStaff(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/staff/{id}
     * Xóa một thông báo của staff
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteNotificationByStaff(id, userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Notification deleted successfully",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/staff
     * Xóa tất cả thông báo của staff hiện tại
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteAllNotificationsByStaff(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa tất cả thông báo thành công",
                "status", "success"
        ));
    }
}