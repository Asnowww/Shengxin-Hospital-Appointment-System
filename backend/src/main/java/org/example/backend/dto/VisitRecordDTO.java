package org.example.backend.dto;

import lombok.Data;

@Data
public class VisitRecordDTO {

    // ========== 预约 / 就诊信息 ==========
    private Long appointmentId;
    private String doctorName;
    private String deptName;
    private String appointmentTime;

    // ========== 病例信息 ==========
    private MedicalRecordDTO medicalRecord;
}
