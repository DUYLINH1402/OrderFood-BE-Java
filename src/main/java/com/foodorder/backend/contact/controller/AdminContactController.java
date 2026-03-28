package com.foodorder.backend.contact.controller;

import com.foodorder.backend.contact.dto.*;
import com.foodorder.backend.contact.entity.ContactStatus;
import com.foodorder.backend.contact.service.ContactService;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.security.annotation.RequireAdmin;
import com.foodorder.backend.security.annotation.RequireStaff;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API quản lý tin nhắn liên hệ dành cho Admin
 * Yêu cầu quyền ADMIN hoặc STAFF
 */
@RestController
@RequestMapping("/api/v1/staff/contacts")
@RequiredArgsConstructor

@Slf4j
@Tag(name = "Contact - Staff", description = "Contact message management API (Staff/Admin)")
@RequireStaff
public class AdminContactController {

    private final ContactService contactService;

    /**
     * Lấy danh sách tất cả tin nhắn liên hệ (phân trang)
     */
    @Operation(summary = "Get all contact messages", description = "Get all contact messages with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getAllContacts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.getAllContacts(pageable));
    }

    /**
     * Lấy danh sách tin nhắn theo trạng thái
     */
    @Operation(summary = "Get messages by status", description = "Filter messages by status: PENDING, READ, REPLIED, ARCHIVED")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ContactResponse>> getContactsByStatus(
            @PathVariable ContactStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.getContactsByStatus(status, pageable));
    }

    /**
     * Lấy danh sách tin nhắn theo nhiều trạng thái
     */
    @Operation(summary = "Get messages by multiple statuses", description = "Filter messages by list of statuses")
    @GetMapping("/statuses")
    public ResponseEntity<Page<ContactResponse>> getContactsByStatuses(
            @RequestParam List<ContactStatus> statuses,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.getContactsByStatuses(statuses, pageable));
    }

    /**
     * Tìm kiếm tin nhắn theo keyword
     */
    @Operation(summary = "Search messages", description = "Search messages by name, email, content or subject")
    @GetMapping("/search")
    public ResponseEntity<Page<ContactResponse>> searchContacts(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.searchContacts(keyword, pageable));
    }

    /**
     * Lấy chi tiết tin nhắn
     */
    @Operation(summary = "Get message details", description = "View contact message details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    /**
     * Cập nhật trạng thái tin nhắn
     */
    @Operation(summary = "Update message status", description = "Update status and note for message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Updated successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactResponse> updateContactStatus(
            @PathVariable Long id,
            @Valid @RequestBody ContactUpdateRequest request) {
        return ResponseEntity.ok(contactService.updateContactStatus(id, request));
    }

    /**
     * Phản hồi tin nhắn liên hệ
     */
    @Operation(summary = "Reply to message", description = "Reply to contact message and optionally send email to customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Replied successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PostMapping("/{id}/reply")
    public ResponseEntity<ContactResponse> replyToContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactReplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long adminId = userDetails.getUser().getId();
        return ResponseEntity.ok(contactService.replyToContact(id, request, adminId));
    }

    /**
     * Xóa tin nhắn (chỉ tin đã archived)
     */
    @Operation(summary = "Delete message", description = "Delete archived message (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Message not archived"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message deleted successfully"
        ));
    }

    /**
     * Đếm số tin nhắn chưa đọc
     */
    @Operation(summary = "Count unread messages", description = "Get number of pending messages")
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Object>> countPendingMessages() {
        long count = contactService.countPendingMessages();
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }

    /**
     * Lấy thống kê tin nhắn liên hệ
     */
    @Operation(summary = "Get message statistics", description = "Get statistics by status and date")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getContactStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(contactService.getContactStatistics(startDate, endDate));
    }

    /**
     * Lấy danh sách tin nhắn mới nhất (cho Dashboard)
     */
    @Operation(summary = "Get recent messages", description = "Get latest messages for Dashboard")
    @GetMapping("/recent")
    public ResponseEntity<List<ContactResponse>> getRecentContacts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(contactService.getRecentContacts(limit));
    }
}

