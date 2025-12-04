package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MedicalRecordDTO {

    private Long recordId;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;

    private String chiefComplaint;
    private String presentIllness;
    private String physicalExam;
    private String diagnosis;
    private String treatment;
    private String doctorAdvice;
    private BigDecimal fee;
    private LocalDate nextVisitDate;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 以下是患者信息，用于前端展示
    private LocalDate birthDate;
    private BigDecimal height;
    private BigDecimal weight;
    private String address;
    private String medicalHistory;
    private String gender;
}
