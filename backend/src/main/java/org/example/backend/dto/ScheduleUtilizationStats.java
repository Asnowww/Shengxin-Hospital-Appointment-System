package org.example.backend.dto;

import lombok.Data;

@Data
public class ScheduleUtilizationStats {
    private Long doctorId;
    private String doctorName;
    private Integer deptId;
    private String deptName;
    private Integer totalSlots;           // 总号源数
    private Integer bookedSlots;          // 已预约号源
    private Double utilizationRate;       // 利用率（百分比）
}
