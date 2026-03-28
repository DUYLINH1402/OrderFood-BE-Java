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
@Tag(name = "Chat - Client", description = "Chat APIs for authenticated users")
public class ChatClientController {

    private final ChatService chatService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Chat history (User)", description = "Retrieve chat history of the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getChatHistory(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
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
            log.error("Error fetching chat history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "CHAT_HISTORY_ERROR",
                "message", "Failed to fetch chat history"
            ));
        }
    }

    @Operation(summary = "Unread messages (User)", description = "Retrieve unread messages for the current user.")
    @ApiResponse(responseCode = "200", description = "Success")
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
            log.error("Error fetching unread messages: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_MESSAGES_ERROR",
                "message", "Failed to fetch unread messages"
            ));
        }
    }

    @Operation(summary = "Mark as read (User)", description = "Mark a message as read.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/mark-read/{messageId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markMessageAsRead(
            @Parameter(description = "Message ID") @PathVariable String messageId) {
        try {
            chatService.markMessageAsRead(messageId);

            return ResponseEntity.ok(Map.of(
                "message", "Message marked as read",
                "messageId", messageId
            ));

        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MARK_READ_ERROR",
                "message", "Failed to mark message as read"
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
        throw new RuntimeException("Unable to authenticate user");
    }
}

