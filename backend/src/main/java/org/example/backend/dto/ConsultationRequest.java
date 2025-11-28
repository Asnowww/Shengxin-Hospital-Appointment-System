package org.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsultationRequest {
    private String message;
    private List<MessageHistory> history;

    @Data
    public static class MessageHistory {
        private String role; // "user" or "model"
        private String content;
    }
}
