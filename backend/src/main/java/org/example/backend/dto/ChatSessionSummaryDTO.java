package org.example.backend.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionSummaryDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;

    private Long doctorId;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private String lastMessage;
    private LocalDateTime lastTime;
    private long unreadCount;
    private String statusLabel;

    private String status;
}
