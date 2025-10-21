package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班详情响应（包含关联信息）
 */
@Data
public class ScheduleDetailVO {
    private Integer scheduleId;
    private Long doctorId;
    private String doctorName; // 医生姓名
    private String doctorTitle; // 医生职称
    private Integer deptId;
    private String deptName; // 科室名称
    private Integer roomId;
    private String roomName; // 诊室名称
    private LocalDate workDate;
    private Integer timeSlot; // 0=上午, 1=下午, 2=晚上
    private String timeSlotName; // 上午/下午/晚上
    private Integer appointmentTypeId;
    private String appointmentTypeName; // 号别名称
    private Integer maxSlots;
    private Integer availableSlots;
    private Integer bookedSlots; // 已预约数
    private String status; // open, full, cancelled
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
