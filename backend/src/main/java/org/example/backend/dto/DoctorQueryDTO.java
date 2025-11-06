package org.example.backend.dto;

import lombok.Data;

@Data
public class DoctorQueryDTO {

    private Integer deptId;
    private String username;
    private String status;
    private String doctorStatus;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}