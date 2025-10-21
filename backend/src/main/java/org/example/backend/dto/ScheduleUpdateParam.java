package org.example.backend.dto;

import lombok.Data;

/**
 * 修改排班的请求参数
 */
@Data
public class ScheduleUpdateParam {
    private Integer scheduleId;
    private Integer roomId; // 可选：修改诊室
    private Integer timeSlot; // 可选：修改时间段
    private Integer maxSlots; // 可选：修改最大号源数
    private String status; // 可选：修改状态（open/cancelled）
    private String cancelReason; // 如果status=cancelled，必须提供原因
}
