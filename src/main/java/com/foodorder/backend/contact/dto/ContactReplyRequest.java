package com.foodorder.backend.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho admin phản hồi tin nhắn liên hệ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactReplyRequest {

    @NotBlank(message = "Reply content cannot be empty")
    @Size(min = 10, max = 5000, message = "Reply content must be between 10 and 5000 characters")
    private String replyContent;

    /**
     * Có gửi email phản hồi cho khách hàng không
     */
    private Boolean sendEmail = true;
}

