package com.foodorder.backend.chat.controller;

import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.entity.Conversation;
import com.foodorder.backend.chat.service.ChatService;
import com.foodorder.backend.chat.service.ConversationService;
import com.foodorder.backend.security.annotation.RequireStaff;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.service.UserService;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller cho Chat - API dành cho Staff và Admin
 * Xem tin nhắn từ user, danh sách user đã chat, đánh dấu đã đọc
 */
@RestController
@RequestMapping("/api/v1/staff/chat")
@RequiredArgsConstructor
@RequireStaff
@Slf4j
@Tag(name = "Chat - Staff", description = "Chat APIs for staff members")
public class ChatStaffController {

    private final ChatService chatService;
    private final ConversationService conversationService;
    private final UserService userService;

    @Operation(summary = "All messages (Staff)", description = "Retrieve all messages from users to staff.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/all-messages")
    public ResponseEntity<?> getAllUserToStaffMessages(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessageResponse> messages = chatService.getAllUserToStaffMessages(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalPages", messages.getTotalPages());
            response.put("totalElements", messages.getTotalElements());
            response.put("hasNext", messages.hasNext());
            response.put("hasPrevious", messages.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching messages for staff: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "STAFF_MESSAGES_ERROR",
                "message", "Failed to fetch messages"
            ));
        }
    }

    @Operation(summary = "User messages (Staff)", description = "Retrieve messages from a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/messages")
    public ResponseEntity<?> getUserMessages(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "USER_NOT_FOUND",
                    "message", "User not found"
                ));
            }

            Pageable pageable = PageRequest.of(page, size);

            // Tìm conversation của user - sử dụng Optional để tránh exception
            Optional<Conversation> conversationOpt = conversationService.findConversationByUser(user);

            Map<String, Object> response = new HashMap<>();

            if (conversationOpt.isPresent()) {
                // Có conversation -> lấy tin nhắn cho staff
                Long conversationId = conversationOpt.get().getId();
                Page<ChatMessageResponse> messages = chatService.getChatHistoryForStaffInConversationPageable(conversationId, pageable);

                response.put("messages", messages.getContent());
                response.put("currentPage", messages.getNumber());
                response.put("totalPages", messages.getTotalPages());
                response.put("totalElements", messages.getTotalElements());
                response.put("hasNext", messages.hasNext());
                response.put("hasPrevious", messages.hasPrevious());
            } else {
                // User chưa có conversation -> trả về kết quả rỗng
                response.put("messages", List.of());
                response.put("currentPage", 0);
                response.put("totalPages", 0);
                response.put("totalElements", 0);
                response.put("hasNext", false);
                response.put("hasPrevious", false);
            }

            response.put("user", Map.of(
                "id", user.getId(),
                "name", user.getFullName(),
                "email", user.getEmail(),
                "phone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching messages of user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "USER_MESSAGES_ERROR",
                "message", "Failed to fetch user messages"
            ));
        }
    }

    @Operation(summary = "Message read status (Staff)", description = "Check unread message count from a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/read-status")
    public ResponseEntity<?> getUserMessageReadStatus(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "USER_NOT_FOUND",
                    "message", "User not found"
                ));
            }

            // Đếm số tin nhắn chưa đọc từ user này mà staff chưa đọc
            Long unreadCount = chatService.countUnreadMessagesFromUserForStaff(user);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("userName", user.getFullName());
            response.put("userEmail", user.getEmail());
            response.put("unreadCount", unreadCount);
            response.put("hasUnreadMessages", unreadCount > 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching unread count for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_COUNT_ERROR",
                "message", "Failed to fetch unread message count"
            ));
        }
    }

    @Operation(summary = "Total unread count (Staff)", description = "Get total unread message count from all users.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            Long unreadCount = chatService.countUnreadUserToStaffMessages();

            return ResponseEntity.ok(Map.of(
                "unreadCount", unreadCount
            ));

        } catch (Exception e) {
            log.error("Error fetching unread count: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_COUNT_ERROR",
                "message", "Failed to fetch unread message count"
            ));
        }
    }

    @Operation(summary = "Users who chatted (Staff)", description = "Retrieve all users who have chatted with staff.")
    @ApiResponse(responseCode = "200", description = "Success")
    @GetMapping("/users")
    public ResponseEntity<?> getUsersChatWithStaff() {
        try {
            List<User> users = chatService.getUsersChatWithStaff();

            // Tạo response với thông tin cần thiết và số tin nhắn chưa đọc cho mỗi user
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Long unreadCount = chatService.countUnreadMessagesFromUserForStaff(user);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("email", user.getEmail());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("avatarUrl", user.getAvatarUrl());
                userInfo.put("unreadCount", unreadCount);
                userInfo.put("hasUnreadMessages", unreadCount > 0);

                return userInfo;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "users", userList,
                "totalUsers", userList.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching chat user list: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "USERS_CHAT_ERROR",
                "message", "Failed to fetch chat user list"
            ));
        }
    }

    @Operation(summary = "Mark as read (Staff)", description = "Mark a message as read.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/mark-read/{messageId}")
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
}

