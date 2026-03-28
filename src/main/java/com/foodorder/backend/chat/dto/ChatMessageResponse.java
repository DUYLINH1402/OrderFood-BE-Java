package com.foodorder.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodorder.backend.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho phản hồi tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing chat message details")
public class ChatMessageResponse {

    @Schema(description = "Message ID", example = "msg_123456")
    private String messageId;

    @Schema(description = "Message content", example = "Hello, I need support")
    private String content;

    @Schema(description = "Sender ID", example = "1")
    private Long senderId;

    @Schema(description = "Sender name", example = "John Doe")
    private String senderName;

    @Schema(description = "Sender email", example = "user@example.com")
    private String senderEmail;

    @Schema(description = "Sender avatar URL", example = "https://example.com/avatar.jpg")
    private String senderAvatar;

    @Schema(description = "Receiver ID", example = "2")
    private Long receiverId;

    @Schema(description = "Receiver name", example = "Staff 01")
    private String receiverName;

    @Schema(description = "Message type", example = "USER_TO_STAFF", allowableValues = {"USER_TO_STAFF", "STAFF_TO_USER", "SYSTEM"})
    private String messageType;

    @Schema(description = "Message status", example = "SENT", allowableValues = {"SENT", "DELIVERED", "READ"})
    private String status;

    @Schema(description = "Sent timestamp", example = "2025-01-20 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @Schema(description = "Read timestamp", example = "2025-01-20 10:31:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    // ========== REPLY REFERENCE FIELDS ==========
    /**
     * ID của tin nhắn gốc mà tin nhắn này đang phản hồi
     */
    @Schema(description = "Original message ID that this message replies to", example = "msg_123455")
    private String replyToMessageId;

    /**
     * Nội dung tin nhắn gốc
     */
    @Schema(description = "Quoted original message content", example = "I want to ask about my order...")
    private String replyToText;

    /**
     * Tên người gửi tin nhắn gốc
     */
    @Schema(description = "Original message sender name", example = "John Doe")
    private String replyToSenderName;

    /**
     * Thời gian gửi tin nhắn gốc
     */
    @Schema(description = "Original message sent timestamp", example = "2025-01-20 10:25:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyToTimestamp;

    /**
     * Chuyển đổi từ Entity sang DTO (không bao gồm reply reference)
     */
    public static ChatMessageResponse fromEntity(ChatMessage chatMessage) {
        ChatMessageResponseBuilder builder = ChatMessageResponse.builder()
                .messageId(chatMessage.getMessageId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSender().getId())
                .senderName(chatMessage.getSender().getFullName())
                .senderEmail(chatMessage.getSender().getEmail())
                .senderAvatar(chatMessage.getSender().getAvatarUrl())
                .messageType(chatMessage.getMessageType().name())
                .status(chatMessage.getStatus().name())
                .sentAt(chatMessage.getSentAt())
                .readAt(chatMessage.getReadAt())
                .replyToMessageId(chatMessage.getReplyToMessageId());

        if (chatMessage.getReceiver() != null) {
            builder.receiverId(chatMessage.getReceiver().getId())
                   .receiverName(chatMessage.getReceiver().getFullName());
        }

        return builder.build();
    }

    /**
     * Chuyển đổi từ Entity sang DTO với đầy đủ thông tin reply reference
     */
    public static ChatMessageResponse fromEntityWithReplyReference(ChatMessage chatMessage, ChatMessage originalMessage) {
        ChatMessageResponse response = fromEntity(chatMessage);
        
        if (originalMessage != null) {
            response.setReplyToText(originalMessage.getContent());
            response.setReplyToSenderName(originalMessage.getSender().getFullName());
            response.setReplyToTimestamp(originalMessage.getSentAt());
        }
        
        return response;
    }
}