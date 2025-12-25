package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("banned_users")
public class BannedUser {
    @TableId(type = IdType.AUTO)
    private Long banId;
    private Long userId;
    private Long patientId;
    private String banType;
    private String banReason;
    private LocalDateTime banStartTime;
    private Integer banDurationWeeks;
    private LocalDateTime banEndTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
