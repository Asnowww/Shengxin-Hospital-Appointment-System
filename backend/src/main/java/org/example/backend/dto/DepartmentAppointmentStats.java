package org.example.backend.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepartmentAppointmentStats {
    private Integer deptId;
    private String deptName;
    private Long appointmentCount;        // 总挂号数
    private Long completedCount;          // 已完成数
    private Long cancelledCount;      // 已取消数
    private BigDecimal totalRevenue;      // 总收入
}
