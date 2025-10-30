package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.pojo.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录查询服务
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 获取用户的通知历史
     * @param userId 用户ID
     * @return 通知列表
     */
    List<Notification> getUserNotifications(Long userId);

    /**
     * 获取待发送的通知
     * @param limit 数量限制
     * @return 待发送通知列表
     */
    List<Notification> getPendingNotifications(int limit);

    /**
     * 获取发送失败的通知
     * @param since 起始时间
     * @return 失败通知列表
     */
    List<Notification> getFailedNotifications(LocalDateTime since);

    /**
     * 重新发送失败的通知
     * @param notificationId 通知ID
     * @return 是否成功
     */
    boolean resendNotification(Long notificationId);

    /**
     * 获取通知统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    NotificationStatistics getNotificationStatistics(Long userId);

    /**
     * 通知统计信息
     */
    class NotificationStatistics {
        private Long totalCount;      // 总数
        private Long sentCount;       // 已发送
        private Long failedCount;     // 发送失败
        private Long pendingCount;    // 待发送

        public NotificationStatistics(Long totalCount, Long sentCount, Long failedCount, Long pendingCount) {
            this.totalCount = totalCount;
            this.sentCount = sentCount;
            this.failedCount = failedCount;
            this.pendingCount = pendingCount;
        }

        // Getters
        public Long getTotalCount() { return totalCount; }
        public Long getSentCount() { return sentCount; }
        public Long getFailedCount() { return failedCount; }
        public Long getPendingCount() { return pendingCount; }
    }
}