package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PatientRegisterParam {
    // 用户信息
    private String username;
    private String password;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String emailCode;

    // 患者专属信息
    private String patientAccount; //教工号/学号
    private String identityType;
    private LocalDate birthDate;
    private String address;
    private BigDecimal height;
    private BigDecimal weight;
    private String medicalHistory;
    private String emergencyContact;
    private String emergencyPhone;
}

