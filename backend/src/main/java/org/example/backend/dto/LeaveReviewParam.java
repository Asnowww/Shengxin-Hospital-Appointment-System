package org.example.backend.dto;

import lombok.Data;

/**
 * 请假审批的请求参数
 */
@Data
public class LeaveReviewParam {
    private Integer leaveId;
    private String action; // approve / reject
    private Long reviewedBy; // 审批人ID
}
