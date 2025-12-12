package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleInfoVO {
    private Integer scheduleId;
    private LocalDate date;
    private Integer timeSlot;
}
