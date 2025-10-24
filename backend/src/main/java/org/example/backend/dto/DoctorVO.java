package org.example.backend.dto;

import lombok.Data;

@Data
public class DoctorVO {
    private Long doctorId;
    private String doctorName;   // 从 users 表取 username
    private String deptName;     // 从 department 表取 deptName
    private String title;
    private String bio;
//    private Integer status;
}
