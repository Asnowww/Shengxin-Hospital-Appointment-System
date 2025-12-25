package org.example.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * 医生申请请假的请求参数
 */
@Data
public class LeaveApplyParam {
    private Long userId;
    /**
     * 选择请假的排班ID列表
     */
    private List<Integer> scheduleIds;
    private String reason;
}
