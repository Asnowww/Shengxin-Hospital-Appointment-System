package org.example.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.ChatMessageDTO;
import org.example.backend.pojo.ChatMessage;
import org.example.backend.pojo.ChatSession;
import org.example.backend.service.ChatMessageService;
import org.example.backend.service.ChatSessionService;
import org.example.backend.service.OnlineStatusService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 文本消息处理器，实现医生与患者之间的在线聊天。
 *
 * 连接约定：
 * ws://{host}/ws/chat?userId={id}&role={doctor|patient}
 *
 * 消息格式（JSON，对应 ChatMessageDTO）：
 * {
 * "sessionId": 1, // 可选，不传则根据 senderType/senderId/receiverId 创建或获取会话
 * "senderId": 123,
 * "senderType": "doctor" | "patient",
 * "receiverId": 456,
 * "content": "你好",
 * "contentType": "text",
 * "appointmentId": 789 // 可选
 * }
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在线连接表，key = role + ":" + userId （例如 doctor:1 / patient:2）
     */
    private final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    private final OnlineStatusService onlineStatusService;

    public ChatWebSocketHandler(ChatMessageService chatMessageService,
            ChatSessionService chatSessionService,
            OnlineStatusService onlineStatusService) {
        this.chatMessageService = chatMessageService;
        this.chatSessionService = chatSessionService;
        this.onlineStatusService = onlineStatusService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, String> params = parseQueryParams(session.getUri());
        String userId = params.get("userId");
        String role = params.get("role");

        if (userId == null || role == null) {
            log.warn("WebSocket 连接缺少必要参数 userId 或 role，关闭连接");
            try {
                session.close(CloseStatus.BAD_DATA);
            } catch (IOException e) {
                log.error("关闭非法 WebSocket 连接异常", e);
            }
            return;
        }

        String key = buildUserKey(role, userId);
        onlineSessions.put(key, session);
        session.getAttributes().put("userRole", role.toLowerCase());
        Long userIdLong = parseLong(userId);
        session.getAttributes().put("userId", userIdLong);
        if ("doctor".equalsIgnoreCase(role)) {
            onlineStatusService.markDoctorOnline(userIdLong);
        } else if ("patient".equalsIgnoreCase(role)) {
            onlineStatusService.markPatientOnline(userIdLong);
        }
        log.info("WebSocket 连接建立: key={}, 当前在线用户数={}", key, onlineSessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            ChatMessageDTO dto = objectMapper.readValue(payload, ChatMessageDTO.class);

            if (dto.getSenderId() == null || dto.getSenderType() == null) {
                log.warn("收到非法聊天消息，缺少 sender 信息: {}", payload);
                return;
            }

            refreshPresence(dto);

            // 1. 会话处理：优先使用已有 sessionId；若没有，则根据 doctorId / patientId 创建或获取
            Long sessionId = dto.getSessionId();
            ChatSession chatSession;
            Long doctorId;
            Long patientId;

            if (sessionId != null) {
                // 根据现有会话直接反查 doctor / patient
                chatSession = chatSessionService.getById(sessionId);
                if (chatSession == null) {
                    log.warn("根据 sessionId={} 未找到 ChatSession，丢弃消息", sessionId);
                    return;
                }
                doctorId = chatSession.getDoctorId();
                patientId = chatSession.getPatientId();
            } else {
                // 兼容旧用法：前端直接传 sender/receiver 组合
                if (dto.getReceiverId() == null) {
                    log.warn("收到缺少 receiverId 且无 sessionId 的消息，无法路由: {}", payload);
                    return;
                }
                if ("doctor".equalsIgnoreCase(dto.getSenderType())) {
                    doctorId = dto.getSenderId();
                    patientId = dto.getReceiverId();
                } else {
                    doctorId = dto.getReceiverId();
                    patientId = dto.getSenderId();
                }
                chatSession = chatSessionService.getOrCreateSession(
                        doctorId,
                        patientId,
                        dto.getAppointmentId());
                sessionId = chatSession.getId();
                dto.setSessionId(sessionId);
            }

            // 补全 receiverId，便于前端和日志使用
            if (dto.getReceiverId() == null) {
                if ("doctor".equalsIgnoreCase(dto.getSenderType())) {
                    dto.setReceiverId(patientId);
                } else {
                    dto.setReceiverId(doctorId);
                }
            }

            // 2. 持久化消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setSenderType(dto.getSenderType());
            chatMessage.setSenderId(dto.getSenderId());
            chatMessage.setContent(dto.getContent());
            chatMessage.setContentType(dto.getContentType() != null ? dto.getContentType() : "text");
            chatMessageService.saveMessage(chatMessage);

            // 3. 准备要发送给前端的消息（可直接使用 DTO，也可补充字段）
            String responseJson = objectMapper.writeValueAsString(dto);
            TextMessage outgoing = new TextMessage(responseJson);

            // 3.1 推送给发送者（用于确认/回显）
            sendToUser(dto.getSenderType(), dto.getSenderId(), outgoing);

            // 3.2 推送给接收者（对端，如果在线）
            String receiverRole = "doctor".equalsIgnoreCase(dto.getSenderType()) ? "patient" : "doctor";
            Long receiverId = "doctor".equalsIgnoreCase(dto.getSenderType()) ? patientId : doctorId;
            sendToUser(receiverRole, receiverId, outgoing);

        } catch (Exception e) {
            log.error("处理 WebSocket 文本消息异常", e);
            try {
                session.sendMessage(new TextMessage("{\"error\":\"invalid_message\"}"));
            } catch (IOException ioException) {
                log.error("发送错误提示消息失败", ioException);
            }
        }
    }

    private void refreshPresence(ChatMessageDTO dto) {
        if (dto.getSenderId() == null) {
            return;
        }
        if ("doctor".equalsIgnoreCase(dto.getSenderType())) {
            onlineStatusService.markDoctorOnline(dto.getSenderId());
        } else if ("patient".equalsIgnoreCase(dto.getSenderType())) {
            onlineStatusService.markPatientOnline(dto.getSenderId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 移除已关闭的连接
        onlineSessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
        Object roleAttr = session.getAttributes().get("userRole");
        Object userIdAttr = session.getAttributes().get("userId");
        if (roleAttr instanceof String role && userIdAttr instanceof Long userId) {
            if ("doctor".equals(role)) {
                onlineStatusService.markDoctorOffline(userId);
            } else if ("patient".equals(role)) {
                onlineStatusService.markPatientOffline(userId);
            }
        }
        log.info("WebSocket 连接关闭，当前在线用户数={}", onlineSessions.size());
    }

    private void sendToUser(String role, Long userId, TextMessage message) {
        if (userId == null) {
            return;
        }
        String key = buildUserKey(role, String.valueOf(userId));
        WebSocketSession targetSession = onlineSessions.get(key);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                targetSession.sendMessage(message);
            } catch (IOException e) {
                log.error("向用户 {} 发送 WebSocket 消息失败", key, e);
            }
        } else {
            log.debug("用户 {} 当前不在线，消息将仅保存在数据库中", key);
        }
    }

    /**
     * 主动关闭指定医生和患者的 WebSocket 连接，用于“结束问诊”时双向断联。
     */
    public void closeUserConnections(Long doctorId, Long patientId) {
        if (doctorId != null) {
            String doctorKey = buildUserKey("doctor", String.valueOf(doctorId));
            WebSocketSession doctorSession = onlineSessions.get(doctorKey);
            if (doctorSession != null && doctorSession.isOpen()) {
                try {
                    doctorSession.close();
                } catch (IOException e) {
                    log.error("关闭医生 WebSocket 连接失败, doctorId={}", doctorId, e);
                }
            }
        }
        if (patientId != null) {
            String patientKey = buildUserKey("patient", String.valueOf(patientId));
            WebSocketSession patientSession = onlineSessions.get(patientKey);
            if (patientSession != null && patientSession.isOpen()) {
                try {
                    patientSession.close();
                } catch (IOException e) {
                    log.error("关闭患者 WebSocket 连接失败, patientId={}", patientId, e);
                }
            }
        }
    }

    private String buildUserKey(String role, String userId) {
        return role.toLowerCase() + ":" + userId;
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> params = new ConcurrentHashMap<>();
        if (uri == null || uri.getQuery() == null) {
            return params;
        }

        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0 && idx < pair.length() - 1) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private Long parseLong(String value) {
        try {
            return value != null ? Long.valueOf(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
