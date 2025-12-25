package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("booking_warnings")
public class BookingWarning {

    @TableId(type = IdType.AUTO)
    private Long warningId;

    private Long patientId;

    private LocalDateTime warningTime;

    private String reason;

    private LocalDateTime createdAt;
}