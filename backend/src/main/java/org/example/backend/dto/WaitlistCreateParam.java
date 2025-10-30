package org.example.backend.dto;

import lombok.Data;

@Data
public class WaitlistCreateParam {
    private Integer scheduleId;
    private String patientId; // 可从token解析
    private Integer priority; // 0-普通，1-紧急，2-非常紧急
    private String notes;
}
