package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.dto.AuditLogQueryDTO;
import org.example.backend.dto.PageResult;
import org.example.backend.pojo.AuditLog;

/**
 * 审计日志服务
 */
public interface AuditLogService extends IService<AuditLog> {

    /**
     * 条件分页查询
     */
    PageResult<AuditLog> queryLogs(AuditLogQueryDTO queryDTO);

    /**
     * 记录一条日志
     */
    AuditLog recordLog(Long userId, AuditLogCreateDTO createDTO);
}
