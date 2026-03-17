package com.foodorder.backend.chat.controller;

import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.service.ChatService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller cho Chat - API dành cho Admin
 * Thống kê chat
 */
@RestController
@RequestMapping("/api/v1/admin/chat")
@RequiredArgsConstructor
@RequireAdmin
@Slf4j
@Tag(name = "Chat - Admin", description = "API quản trị chat")
public class ChatAdminController {

    private final ChatService chatService;

    @Operation(summary = "Thống kê chat (Admin)", description = "Lấy thống kê chat trong khoảng thời gian cụ thể.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/statistics")
    public ResponseEntity<?> getChatStatistics(
            @Parameter(description = "Ngày bắt đầu") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Ngày kết thúc") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<ChatMessageResponse> messages = chatService.getMessagesBetweenDates(startDate, endDate);

            long userToStaffCount = messages.stream()
                    .filter(msg -> "USER_TO_STAFF".equals(msg.getMessageType()))
                    .count();

            long staffToUserCount = messages.stream()
                    .filter(msg -> "STAFF_TO_USER".equals(msg.getMessageType()))
                    .count();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalMessages", messages.size());
            statistics.put("userToStaffMessages", userToStaffCount);
            statistics.put("staffToUserMessages", staffToUserCount);
            statistics.put("startDate", startDate);
            statistics.put("endDate", endDate);
            statistics.put("messages", messages);

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê chat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "STATISTICS_ERROR",
                "message", "Lỗi khi lấy thống kê chat"
            ));
        }
    }
}

