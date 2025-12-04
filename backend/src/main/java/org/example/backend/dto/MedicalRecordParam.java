package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MedicalRecordParam {

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
}
