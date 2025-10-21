package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("appointments")
public class Appointment {

    @TableId(value = "appointment_id", type = IdType.AUTO)
    private Long appointmentId;

    @TableField("patient_id")
    private Long patientId;

    @TableField("schedule_id")
    private Integer scheduleId;

    @TableField("dept_id")
    private Integer deptId;

    @TableField("room_id")
    private Integer roomId;

    @TableField("appointment_type_id")
    private Integer appointmentTypeId;

    @TableField("queue_number")
    private Integer queueNumber;

    @TableField("payment_status")
    private String paymentStatus; // unpaid, paid, expired, refunded

    @TableField("appointment_status")
    private String appointmentStatus; // pending, booked, cancelled, completed, no_show

    @TableField("fee_original")
    private BigDecimal feeOriginal;

    @TableField("fee_final")
    private BigDecimal feeFinal;

    @TableField("booking_time")
    private LocalDateTime bookingTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("visit_time")
    private LocalDateTime visitTime;

    private String notes;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
