package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.ChatSessionSummaryDTO;
import org.example.backend.mapper.ChatSessionMapper;
import org.example.backend.mapper.PatientMapper;
import org.example.backend.mapper.UserMapper;
import org.example.backend.pojo.ChatMessage;
import org.example.backend.pojo.ChatSession;
import org.example.backend.pojo.Patient;
import org.example.backend.pojo.User;
import org.example.backend.service.ChatMessageService;
import org.example.backend.service.ChatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public ChatSession getOrCreateSession(Long doctorId, Long patientId, Long appointmentId) {
        ChatSession session = this.getOne(new QueryWrapper<ChatSession>()
                .eq("doctor_id", doctorId)
                .eq("patient_id", patientId)
                .eq("appointment_id", appointmentId));
        if (session != null)
            return session;

        session = new ChatSession();
        session.setDoctorId(doctorId);
        session.setPatientId(patientId);
        session.setAppointmentId(appointmentId);
        session.setStatus("active");
        this.save(session); // 插入后 id 会回写

        return session;
    }

    @Override
    public ChatSession getById(Long id) {
        return super.getById(id);
    }

    @Override
    public IPage<ChatSession> listSessionsForDoctor(Long doctorId, int page, int size) {
        Page<ChatSession> pg = new Page<>(page, size);
        return this.page(pg, new QueryWrapper<ChatSession>()
                .eq("doctor_id", doctorId)
                .orderByDesc("updated_at"));
    }

    @Override
    public IPage<ChatSession> listSessionsForPatient(Long patientId, int page, int size) {
        Page<ChatSession> pg = new Page<>(page, size);
        return this.page(pg, new QueryWrapper<ChatSession>()
                .eq("patient_id", patientId)
                .orderByDesc("updated_at"));
    }

    @Override
    public void closeSession(Long sessionId) {
        ChatSession s = new ChatSession();
        s.setId(sessionId);
        s.setStatus("closed");
        this.updateById(s);
    }

    @Override
    public List<ChatSessionSummaryDTO> listSessionSummariesForDoctor(Long doctorId) {
        List<ChatSession> sessions = this.list(new QueryWrapper<ChatSession>()
                .eq("doctor_id", doctorId)
                .orderByDesc("updated_at"));
        List<ChatSessionSummaryDTO> result = new ArrayList<>();
        for (ChatSession session : sessions) {
            ChatSessionSummaryDTO dto = new ChatSessionSummaryDTO();
            dto.setId(session.getId());
            dto.setSessionId(session.getId());
            dto.setDoctorId(session.getDoctorId());
            dto.setPatientId(session.getPatientId());
            dto.setPatientName(resolvePatientName(session.getPatientId()));
            dto.setAppointmentId(session.getAppointmentId());
            dto.setStatus(session.getStatus());

            ChatMessage lastMessage = chatMessageService.getLatestMessage(session.getId());
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastTime(lastMessage.getCreatedAt());
            } else {
                dto.setLastMessage(null);
                dto.setLastTime(session.getUpdatedAt());
            }

            long unread = chatMessageService.countUnread(session.getId(), doctorId);
            dto.setUnreadCount(unread);
            dto.setStatusLabel(unread > 0 ? "未读" : "已读");
            result.add(dto);
        }
        return result;
    }

    private String resolvePatientName(Long patientId) {
        if (patientId == null) {
            return "患者";
        }
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null || patient.getUserId() == null) {
            return "患者";
        }
        User user = userMapper.selectById(patient.getUserId());
        return user != null ? user.getName() : "患者";
    }
}
