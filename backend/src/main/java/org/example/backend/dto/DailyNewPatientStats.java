package org.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyNewPatientStats {
    private LocalDate date;
    private Long newPatientCount;         // 新增患者数
}
