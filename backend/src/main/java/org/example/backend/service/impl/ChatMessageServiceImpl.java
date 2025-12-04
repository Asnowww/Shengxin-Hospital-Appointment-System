package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.mapper.ChatMessageMapper;
import org.example.backend.mapper.ChatSessionMapper;
import org.example.backend.pojo.ChatMessage;
import org.example.backend.pojo.ChatSession;
import org.example.backend.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Override
    @Transactional
    public void saveMessage(ChatMessage message) {
        message.setIsRead(false);
        this.save(message);

        // 更新会话的 updated_at
        ChatSession session = new ChatSession();
        session.setId(message.getSessionId());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }

    @Override
    public IPage<ChatMessage> pageMessages(Long sessionId, int page, int size) {
        Page<ChatMessage> pg = new Page<>(page, size);
        return this.page(pg, new QueryWrapper<ChatMessage>()
                .eq("session_id", sessionId)
                .orderByAsc("created_at"));
    }

    @Override
    public void markMessagesRead(Long sessionId, Long userId) {
        // 假设标记为已读是指对方发给当前用户的所有未读消息
        UpdateWrapper<ChatMessage> uw = new UpdateWrapper<>();
        uw.eq("session_id", sessionId)
                .eq("is_read", false)
                // sender不是当前用户 => 表示对方发的消息
                .ne("sender_id", userId);
        ChatMessage update = new ChatMessage();
        update.setIsRead(true);
        this.update(update, uw);
    }

    @Override
    public long countUnread(Long sessionId, Long userId) {
        return this.count(new QueryWrapper<ChatMessage>()
                .eq("session_id", sessionId)
                .eq("is_read", false)
                .ne("sender_id", userId));
    }

    @Override
    public ChatMessage getLatestMessage(Long sessionId) {
        return this.getOne(new QueryWrapper<ChatMessage>()
                .eq("session_id", sessionId)
                .orderByDesc("created_at")
                .last("limit 1"));
    }
}
