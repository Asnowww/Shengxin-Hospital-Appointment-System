package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("waitlist")
public class Waitlist {

    @TableId(type = IdType.AUTO)
    private Long waitId;

    private Integer scheduleId;

    private Long patientId;

    private LocalDateTime requestedAt;

    private Integer priority; // 优先级：0-普通，1-紧急，2-非常紧急

    private String status; // waiting, notified, converted, cancelled

    private LocalDateTime notifiedAt;

    private Long convertedAppointmentId; // 转换成的正式预约ID
}