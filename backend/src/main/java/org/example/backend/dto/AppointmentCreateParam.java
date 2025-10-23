package org.example.backend.dto;

import lombok.Data;

/**
 * 创建预约请求参数
 */
@Data
public class AppointmentCreateParam {
    private Long patientId;              // 患者ID（必填）
    private Integer scheduleId;          // 排班ID（必填）
    private String notes;                // 备注信息（可选）
}