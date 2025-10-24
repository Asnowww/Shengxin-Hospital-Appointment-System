package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppointmentInfoDTO {
    private Long appointmentId;     // 预约ID
    private Long patientId;         // 患者ID
    private String patientName;     // 患者姓名
    private String doctorName;      // 医生姓名
    private String departmentName;  // 科室名称
    private String room_name;       // 诊室名称
    private LocalDateTime appointmentTime; // 就诊时间
    private String status;          // 预约状态（pending, booked, cancelled...）
    private BigDecimal feeFinal;        // 最终费用
    private String remarks;         // 备注
}
