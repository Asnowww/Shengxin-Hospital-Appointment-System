package org.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorAccountDTO {

    // 医生表字段
    private Long doctorId;
    private Long userId;
    private Integer deptId;
    private String deptName;
    private String title;
    private String bio;
    private String doctorStatus;

    // 用户表字段
    private String username;
    private String phone;
    private String email;
    private String gender;
    private String userStatus;

    // 时间字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}