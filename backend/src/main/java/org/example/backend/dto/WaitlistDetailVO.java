package org.example.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 候补详细信息VO
 * 包含候补记录和关联的排班信息
 */
@Data
public class WaitlistDetailVO {
    // 候补基本信息
    private Long waitId;
    private Integer scheduleId;
    private Long patientId;
    private Integer priority;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime notifiedAt;
    private Long convertedAppointmentId;

    // 排班信息
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private Integer deptId;
    private String deptName;
    private LocalDate workDate;
    private Integer timeSlot;
    private String timeSlotName;
    private Integer maxSlots;
    private Integer availableSlots;
    private Integer bookedSlots;
    private String scheduleStatus;
    private Integer roomId;
    private String roomName;
    private Integer appointmentTypeId;
    private String appointmentTypeName;

    // 候补队列统计信息
    private Integer queuePosition;        // 当前候补在队列中的位置
    private Integer totalWaiting;         // 该排班总候补人数
    private Integer highPriorityCount;    // 高优先级候补人数
    private Integer myPriorityRank;       // 我的优先级排名（同优先级按时间）
}