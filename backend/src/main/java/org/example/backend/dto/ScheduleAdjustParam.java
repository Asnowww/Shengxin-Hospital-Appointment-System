package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 医生申请调班的请求参数
 */
@Data
public class ScheduleAdjustParam {
    private Integer scheduleId; // 原排班ID
    private LocalDate adjustedDate; // 调整后的日期
    private Integer adjustedTimeSlot; // 调整后的时间段
    private Integer adjustedRoomId; // 调整后的诊室
    private String reason; // 调班理由
    private Long appliedBy; // 申请人ID
}
