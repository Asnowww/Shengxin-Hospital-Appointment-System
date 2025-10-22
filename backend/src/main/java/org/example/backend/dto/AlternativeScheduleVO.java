package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 推荐的替代排班
 */
@Data
public class AlternativeScheduleVO {
    private Integer scheduleId;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String deptName;
    private String roomName;
    private LocalDate workDate;
    private Integer timeSlot;
    private String timeSlotName;
    private String appointmentTypeName;
    private Integer availableSlots;
}
