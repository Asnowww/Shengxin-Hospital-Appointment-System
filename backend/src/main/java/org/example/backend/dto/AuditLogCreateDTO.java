package org.example.backend.dto;

import lombok.Data;

/**
 * 审计日志创建请求
 */
@Data
public class AuditLogCreateDTO {
    /**
     * 操作类型：create/update/delete/login/logout/approve/reject
     */
    private String action;

    /**
     * 资源类型：user/doctor/appointment/schedule/payment/refund/rule/system
     */
    private String resourceType;

    private Long resourceId;

    /**
     * 额外说明
     */
    private String message;

    /**
     * 客户端 IP，可为空
     */
    private String ip;
}
