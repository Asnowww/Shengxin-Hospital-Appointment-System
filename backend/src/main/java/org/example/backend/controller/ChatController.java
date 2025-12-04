package org.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.backend.config.ChatWebSocketHandler;
import org.example.backend.dto.ChatSessionSummaryDTO;
import org.example.backend.pojo.ChatMessage;
import org.example.backend.pojo.ChatSession;
import org.example.backend.pojo.Patient;
import org.example.backend.service.ChatMessageService;
import org.example.backend.service.ChatSessionService;
import org.example.backend.service.PatientService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final PatientService patientService;
    private final ChatWebSocketHandler chatWebSocketHandler;
    // private final JwtUtil jwtUtil;

    @GetMapping("/session")
    public ChatSession getOrCreateSession(@RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam(required = false) Long appointmentId) {

        return chatSessionService.getOrCreateSession(doctorId, patientId, appointmentId);
    }

    @GetMapping("/messages")
    public IPage<ChatMessage> pageMessages(@RequestParam Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatMessageService.pageMessages(sessionId, page, size);
    }

    @PostMapping("/session/close")
    public void closeSession(@RequestParam Long sessionId) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session != null) {
            chatSessionService.closeSession(sessionId);
            chatWebSocketHandler.closeUserConnections(session.getDoctorId(), session.getPatientId());
        }
    }

    @PostMapping("/messages/read")
    public void markRead(@RequestParam Long sessionId,
            @RequestParam Long userId) {
        chatMessageService.markMessagesRead(sessionId, userId);
    }

    @GetMapping("/sessions/doctor/{doctorId}")
    public java.util.List<ChatSessionSummaryDTO> listDoctorSessions(@PathVariable Long doctorId) {
        return chatSessionService.listSessionSummariesForDoctor(doctorId);
    }
}
