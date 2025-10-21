package org.example.backend.dto;

import lombok.Data;

/**
 * 临时加号的请求参数
 */
@Data
public class AddExtraSlotsParam {
    private Integer scheduleId;
    private Integer extraSlots; // 增加的号源数
    private String reason; // 加号原因
    private Long createdBy; // 操作人ID
}
