package org.example.backend.dto;

import lombok.Data;

@Data
public class ScheduleCoverageStats {
    private Integer deptId;            // 科室ID
    private String deptName;           // 科室名称
    private Integer totalDays;         // 统计范围内的总天数
    private Integer scheduledDays;     // 该科室有排班的天数
    private Double coverageRate;       // 排班覆盖率（%）
}