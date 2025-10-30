package org.example.backend.controller;

import jakarta.annotation.Resource;
import org.example.backend.pojo.Notification;
import org.example.backend.service.NotificationService;
import org.example.backend.dto.Result;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Resource
    private TokenUtil tokenUtil;

    /**
     * 获取我的通知记录
     * GET /notifications/my
     */
    @GetMapping("/my")
    public Result<List<Notification>> getMyNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            if (token == null) {
                return Result.error("未提供 token");
            }

            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return Result.success(notifications);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取通知详情
     * GET /notifications/{id}
     */
    @GetMapping("/{id}")
    public Result<Notification> getNotificationDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            if (token == null) {
                return Result.error("未提供 token");
            }

            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            Notification notification = notificationService.getById(id);
            if (notification == null) {
                return Result.error("通知不存在");
            }

            // 验证权限
            if (!notification.getUserId().equals(userId)) {
                return Result.error("无权查看该通知");
            }

            return Result.success(notification);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取通知统计信息
     * GET /notifications/statistics
     */
    @GetMapping("/statistics")
    public Result<NotificationService.NotificationStatistics> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam
    ) {
        try {
            String token = tokenUtil.extractToken(authHeader, tokenParam);
            if (token == null) {
                return Result.error("未提供 token");
            }

            Long userId = tokenUtil.resolveUserIdFromToken(token);
            if (userId == null) {
                return Result.error("token 无效或已过期");
            }

            NotificationService.NotificationStatistics statistics =
                    notificationService.getNotificationStatistics(userId);
            return Result.success(statistics);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 重新发送失败的通知（管理员功能）
     * POST /notifications/resend/{id}
     */
    @PostMapping("/resend/{id}")
    public Result<Void> resendNotification(@PathVariable Long id) {
        try {
            boolean success = notificationService.resendNotification(id);
            if (success) {
                return Result.success("重新发送成功", null);
            } else {
                return Result.error("重新发送失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("重新发送失败：" + e.getMessage());
        }
    }

    /**
     * 获取待发送的通知列表（管理员功能）
     * GET /notifications/pending
     */
    @GetMapping("/pending")
    public Result<List<Notification>> getPendingNotifications(
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        try {
            List<Notification> notifications = notificationService.getPendingNotifications(limit);
            return Result.success(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取发送失败的通知列表（管理员功能）
     * GET /notifications/failed
     */
    @GetMapping("/failed")
    public Result<List<Notification>> getFailedNotifications(
            @RequestParam(value = "days", defaultValue = "7") int days
    ) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<Notification> notifications = notificationService.getFailedNotifications(since);
            return Result.success(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}