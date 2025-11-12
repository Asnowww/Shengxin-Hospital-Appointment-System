package org.example.backend.dto;

import lombok.Data;

@Data
public class DoctorAttendanceStats {
    private Long doctorId;
    private String doctorName;
    private Integer deptId;
    private String deptName;
    private Integer scheduledCount;       // 计划排班次数
    private Integer attendedCount;        // 实际出诊次数（状态为open或full）
    private Double attendanceRate;        // 出诊率（百分比）
}