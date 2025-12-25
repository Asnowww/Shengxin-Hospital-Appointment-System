package org.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.backend.config.ChatWebSocketHandler;
import org.example.backend.dto.ChatSessionSummaryDTO;
import org.example.backend.pojo.ChatMessage;
import org.example.backend.pojo.ChatSession;
import org.example.backend.service.ChatMessageService;
import org.example.backend.service.ChatSessionService;
import org.example.backend.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (!isParticipant(session, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user is not a participant of this session");
        }
        return chatMessageService.pageMessages(sessionId, page, size);
    }

    @PostMapping("/session/close")
    public void closeSession(@RequestParam Long sessionId,
            @RequestParam Long userId) {
        ChatSession session = chatSessionService.getById(sessionId);
        // 验证用户是会话的参与方
        if (session != null && (session.getDoctorId().equals(userId) || session.getPatientId().equals(userId))) {
            chatSessionService.closeSession(sessionId);
            chatWebSocketHandler.notifySessionClosed(session.getDoctorId(), session.getPatientId(), sessionId);
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

    /**
     * 检查用户是否是会话的参与者（医生或患者）
     */
    private boolean isParticipant(ChatSession session, Long userId) {
        if (session == null || userId == null) {
            return false;
        }
        return userId.equals(session.getDoctorId()) || userId.equals(session.getPatientId());
    }
}
