package org.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者个人信息修改参数
 */
@Data
public class PatientUpdateParam {

    // 用户基础信息
    // private String username; // 姓名
    private String gender; // 性别
    private String phone; // 手机号
    private String email; // 邮箱

    // 患者专属信息
    private String patientAccount; // 学号/教工号
    private String identityType; // 身份类型：student/teacher/staff
    private LocalDate birthDate; // 出生日期
    private String address; // 家庭地址
    private BigDecimal height; // 身高(cm)
    private BigDecimal weight; // 体重(kg)
    private String medicalHistory; // 既往病史
    private String emergencyContact; // 紧急联系人姓名
    private String emergencyPhone; // 紧急联系人电话
}
