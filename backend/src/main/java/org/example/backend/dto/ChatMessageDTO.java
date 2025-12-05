package org.example.backend.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long sessionId;
    private Long senderId;
    private String senderType;
    private Long receiverId; // 目标用户 ID
    private String content;
    private String contentType;
    private Long appointmentId; // 可选
}
