package org.example.backend.dto;

import lombok.Data;

@Data
public class DoctorVO {
    private Long doctorId;
    private Integer deptId;
    private String email;
    private String phone;
    private String doctorName; // 从 users 表取 username
    private String deptName; // 从 department 表取 deptName
    private String title;
    private String bio;
    private String status;
    private String bioStatus;
    /**
     * 在线状态：online / offline / busy 等，由 WebSocket 在线表实时计算
     */
    private String onlineStatus;
}
