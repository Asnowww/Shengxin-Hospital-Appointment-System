package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.mapper.NotificationMapper;
import org.example.backend.pojo.Notification;
import org.example.backend.service.NotificationService;
import org.example.backend.util.EmailAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录服务实现类
 */
@Service
@Slf4j
public class NotificationServiceImpl
        extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Autowired
    private EmailAPI emailAPI;

    /**
     * 获取用户的通知历史
     */
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return baseMapper.selectList(
                new QueryWrapper<Notification>()
                        .lambda()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt)
        );
    }

    /**
     * 获取待发送的通知
     */
    @Override
    public List<Notification> getPendingNotifications(int limit) {
        return baseMapper.selectList(
                new QueryWrapper<Notification>()
                        .lambda()
                        .eq(Notification::getStatus, "pending")
                        .orderByAsc(Notification::getCreatedAt)
                        .last("LIMIT " + limit)
        );
    }

    /**
     * 获取发送失败的通知（since之后）
     */
    @Override
    public List<Notification> getFailedNotifications(LocalDateTime since) {
        return baseMapper.selectList(
                new QueryWrapper<Notification>()
                        .lambda()
                        .eq(Notification::getStatus, "failed")
                        .ge(Notification::getCreatedAt, since)
                        .orderByDesc(Notification::getCreatedAt)
        );
    }

    @Override
    public boolean resendNotification(Long notificationId) {
        Notification notification = baseMapper.selectById(notificationId);
        if (notification == null) {
            log.warn("通知ID {} 不存在", notificationId);
            return false;
        }

        try {
            log.info("重新发送邮件通知给用户 {}，主题：{}", notification.getUserId(), notification.getSubject());

            boolean result = emailAPI.sendHtmlEmail(
                    notification.getSubject(),
                    notification.getContent(),
                    notification.getEmail()
            ).join(); // 因为 sendHtmlEmail 是异步的，join() 用于等待结果

            // 更新通知状态
            if (result) {
                notification.setStatus("sent");
                notification.setSentAt(LocalDateTime.now());
                baseMapper.updateById(notification);
                log.info("通知 {} 已成功重新发送", notificationId);
                return true;
            } else {
                notification.setStatus("failed");
                baseMapper.updateById(notification);
                log.warn("通知 {} 发送失败", notificationId);
                return false;
            }

        } catch (Exception e) {
            log.error("重新发送通知失败: {}", e.getMessage(), e);
            notification.setStatus("failed");
            baseMapper.updateById(notification);
            return false;
        }
    }


    /**
     * 获取通知统计信息
     */
    @Override
    public NotificationStatistics getNotificationStatistics(Long userId) {
        long totalCount = count(new QueryWrapper<Notification>().lambda().eq(Notification::getUserId, userId));
        long sentCount = count(new QueryWrapper<Notification>().lambda()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, "sent"));
        long failedCount = count(new QueryWrapper<Notification>().lambda()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, "failed"));
        long pendingCount = count(new QueryWrapper<Notification>().lambda()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, "pending"));

        return new NotificationStatistics(totalCount, sentCount, failedCount, pendingCount);
    }
}