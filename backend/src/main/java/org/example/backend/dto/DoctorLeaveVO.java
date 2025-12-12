package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生请假记录响应
 */
@Data
public class DoctorLeaveVO {
    private Integer leaveId;
    private Long doctorId;
    private String doctorName; // 医生姓名
    private String deptName; // 科室名称
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status; // pending, approved, rejected
    private Long appliedBy;
    private String appliedByName; // 申请人姓名
    private LocalDateTime appliedAt;
    private Long reviewedBy;
    private String reviewedByName; // 审批人姓名
    private LocalDateTime reviewedAt;
    private Integer affectedScheduleCount; // 受影响的排班数量
    private Integer affectedAppointmentCount; // 受影响的挂号数量
}
