package org.example.backend.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者信息表
 */
@Data
@TableName("patients")
public class Patient {

    /** 主键ID */
    @TableId(value = "patient_id", type = IdType.AUTO)
    private Long patientId;

    /** 对应的用户ID（外键） */
    @TableField("user_id")
    private Long userId;

    @TableField("patient_account")
    private String patientAccount;

    /** 身份类型：student / teacher / staff */
    @TableField("identity_type")
    private String identityType;

    /** 出生日期 */
    @TableField("birth_date")
    private LocalDate birthDate;

    /** 家庭住址 */
    @TableField("address")
    private String address;

    /** 身高（cm），保留两位小数 */
    @TableField("height")
    private BigDecimal height;

    /** 体重（kg），保留两位小数 */
    @TableField("weight")
    private BigDecimal weight;

    /** 既往病史 */
    @TableField("medical_history")
    private String medicalHistory;

    /** 紧急联系人姓名 */
    @TableField("emergency_contact")
    private String emergencyContact;

    /** 紧急联系人电话 */
    @TableField("emergency_phone")
    private String emergencyPhone;
}

