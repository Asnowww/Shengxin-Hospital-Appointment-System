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
    private String doctorTitle;      //医生职称
    private String doctorInfo;      //医生信息
    private String deptName;  // 科室名称
    private String roomName;       // 诊室名称
    private String typeName;        //门诊类别
    private String appointmentTime; // 预约就诊时间
    private String bookingTime;     // 预约创建时间
    private String status;          // 预约状态（pending, booked, cancelled...）
    private BigDecimal feeFinal;        // 最终费用
    private String remarks;         // 备注
}
