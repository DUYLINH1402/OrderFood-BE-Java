package com.foodorder.backend.chat.controller;

import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.service.ChatService;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller cho Chat - API dành cho User đã đăng nhập
 * Xem lịch sử chat, tin nhắn chưa đọc, đánh dấu đã đọc
 */
@RestController
@RequestMapping("/api/v1/client/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat - Client", description = "API chat dành cho người dùng đã đăng nhập")
public class ChatClientController {

    private final ChatService chatService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Lịch sử chat (User)", description = "Lấy lịch sử chat của user hiện tại.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getChatHistory(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            Pageable pageable = PageRequest.of(page, size);

            Page<ChatMessageResponse> chatHistory = chatService.getChatHistoryForUserPageable(currentUser, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", chatHistory.getContent());
            response.put("currentPage", chatHistory.getNumber());
            response.put("totalPages", chatHistory.getTotalPages());
            response.put("totalElements", chatHistory.getTotalElements());
            response.put("hasNext", chatHistory.hasNext());
            response.put("hasPrevious", chatHistory.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử chat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "CHAT_HISTORY_ERROR",
                "message", "Lỗi khi lấy lịch sử chat"
            ));
        }
    }

    @Operation(summary = "Tin nhắn chưa đọc (User)", description = "Lấy danh sách tin nhắn chưa đọc của user hiện tại.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/unread")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUnreadMessages(
            @Parameter(hidden = true) HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            List<ChatMessageResponse> unreadMessages = chatService.getUnreadMessagesForUser(currentUser);

            return ResponseEntity.ok(Map.of(
                "unreadMessages", unreadMessages,
                "count", unreadMessages.size()
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn chưa đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_MESSAGES_ERROR",
                "message", "Lỗi khi lấy tin nhắn chưa đọc"
            ));
        }
    }

    @Operation(summary = "Đánh dấu đã đọc (User)", description = "Đánh dấu một tin nhắn là đã đọc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tin nhắn")
    })
    @PutMapping("/mark-read/{messageId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markMessageAsRead(
            @Parameter(description = "ID tin nhắn") @PathVariable String messageId) {
        try {
            chatService.markMessageAsRead(messageId);

            return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tin nhắn là đã đọc",
                "messageId", messageId
            ));

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tin nhắn đã đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MARK_READ_ERROR",
                "message", "Lỗi khi đánh dấu tin nhắn đã đọc"
            ));
        }
    }

    // ========== HELPER METHODS ==========

    private User getCurrentUser(HttpServletRequest request) {
        String token = jwtUtil.getTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            return userService.findByUsername(username);
        }
        throw new RuntimeException("Không thể xác thực người dùng");
    }
}

