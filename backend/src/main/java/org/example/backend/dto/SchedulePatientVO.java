package org.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SchedulePatientVO {
    /** 预约ID */
    private Long appointmentId;

    /** 患者ID */
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 性别 */
    private String gender;

    /** 年龄 */
    private Integer age;

    /** 电话 */
    private String phone;

    /** 原始排队号*/
    private Integer queueNumber;

    /**候诊序号*/
    private Integer waitingNumber;

    /** 预约状态 */
    private String appointmentStatus;

    /** 预约时间 */
    private Integer bookingTime;

    /** 预约类型名称 */
    private String appointmentTypeName;
}