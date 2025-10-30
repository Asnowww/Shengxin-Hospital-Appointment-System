package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件通知记录实体类
 */
@Data
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long notificationId;

    private Long userId;

    private String email; // 冗余存储，避免频繁JOIN

    private String subject;

    private String content;

    private String status; // pending, sent, failed

    private LocalDateTime sentAt;

    private LocalDateTime createdAt;
}