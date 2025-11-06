package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量创建排班的请求参数
 */
@Data
public class ScheduleCreateParam {
    private Long doctorId;
    private Integer deptId;
    private Integer roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> timeSlots; // 0=上午, 1=下午, 2=晚上
    private List<Integer> weekdays; // 1=周一, 2=周二, ..., 7=周日 (用于按周模板创建)
    private Integer appointmentTypeId;
    private Integer maxSlots;
}
