package org.example.backend.dto;

import lombok.Data;

/**
 * 修改预约请求参数
 */
@Data
public class AppointmentUpdateParam {
    private Long appointmentId;         // 预约ID（必填）
    private Long patientId;             // 患者ID（用于验证权限）
    private Integer newScheduleId;      // 新的排班ID（改期改时间）
    private String notes;               // 备注信息
    private String paymentStatus;       // 支付状态（unpaid, paid, expired, refunded）
    private String appointmentStatus;   // 预约状态（pending, booked, cancelled, completed, no_show）
}
