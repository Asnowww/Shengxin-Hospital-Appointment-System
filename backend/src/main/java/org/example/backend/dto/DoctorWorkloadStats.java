package org.example.backend.dto;

import lombok.Data;

@Data
public class DoctorWorkloadStats {
    private Long doctorId;
    private String doctorName;
    private Integer deptId;
    private String deptName;
    private Integer scheduleCount;        // 排班次数
    private Long appointmentCount;        // 预约数量
    private Integer totalSlots;           // 总号源数
    private Integer availableSlots;       // 剩余号源数
    private Integer bookedSlots;          // 已预约号源
}
