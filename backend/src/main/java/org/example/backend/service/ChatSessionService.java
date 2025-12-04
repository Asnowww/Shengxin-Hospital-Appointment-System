package org.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.backend.dto.ChatSessionSummaryDTO;
import org.example.backend.pojo.ChatSession;

// interface
public interface ChatSessionService {
    ChatSession getOrCreateSession(Long doctorId, Long patientId, Long appointmentId);

    ChatSession getById(Long id);

    IPage<ChatSession> listSessionsForDoctor(Long doctorId, int page, int size);

    IPage<ChatSession> listSessionsForPatient(Long patientId, int page, int size);

    void closeSession(Long sessionId);

    java.util.List<ChatSessionSummaryDTO> listSessionSummariesForDoctor(Long doctorId);
}
