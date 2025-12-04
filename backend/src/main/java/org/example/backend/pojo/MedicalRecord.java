package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者病历表
 */
@Data
@TableName("medical_records")
public class MedicalRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @TableField("patient_id")
    private Long patientId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("appointment_id")
    private Long appointmentId;

    @TableField("chief_complaint")
    private String chiefComplaint;

    @TableField("present_illness")
    private String presentIllness;

    @TableField("physical_exam")
    private String physicalExam;

    @TableField("diagnosis")
    private String diagnosis;

    @TableField("treatment")
    private String treatment;

    @TableField("doctor_advice")
    private String doctorAdvice;

    @TableField("fee")
    private BigDecimal fee;

    @TableField("next_visit_date")
    private LocalDate nextVisitDate;

    @TableField("notes")
    private String notes;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
