package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 医生申请请假的请求参数
 */
@Data
public class LeaveApplyParam {
    private Long doctorId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
}
