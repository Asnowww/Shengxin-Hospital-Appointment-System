package org.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.backend.pojo.ChatMessage;

public interface ChatMessageService {
    void saveMessage(ChatMessage message);

    IPage<ChatMessage> pageMessages(Long sessionId, int page, int size);

    void markMessagesRead(Long sessionId, Long userId); // 标记 session 中某用户的消息为已读

    long countUnread(Long sessionId, Long userId);

    ChatMessage getLatestMessage(Long sessionId);
}
