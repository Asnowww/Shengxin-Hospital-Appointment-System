package org.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.dto.AuditLogQueryDTO;
import org.example.backend.dto.PageResult;
import org.example.backend.mapper.AuditLogMapper;
import org.example.backend.pojo.AuditLog;
import org.example.backend.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Override
    public PageResult<AuditLog> queryLogs(AuditLogQueryDTO queryDTO) {
        Page<AuditLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getUserId() != null) {
            wrapper.eq(AuditLog::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.isNotBlank(queryDTO.getAction())) {
            wrapper.eq(AuditLog::getAction, queryDTO.getAction());
        }
        if (StringUtils.isNotBlank(queryDTO.getResourceType())) {
            wrapper.eq(AuditLog::getResourceType, queryDTO.getResourceType());
        }
        if (queryDTO.getResourceId() != null) {
            wrapper.eq(AuditLog::getResourceId, queryDTO.getResourceId());
        }
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        if (startTime != null) {
            wrapper.ge(AuditLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getCreatedAt, endTime);
        }
        wrapper.orderByDesc(AuditLog::getCreatedAt);

        Page<AuditLog> result = this.page(page, wrapper);
        return new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                (int) result.getCurrent(),
                (int) result.getSize());
    }

    @Override
    public AuditLog recordLog(Long userId, AuditLogCreateDTO createDTO) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(createDTO.getAction());
        log.setResourceType(createDTO.getResourceType());
        log.setResourceId(createDTO.getResourceId());
        log.setMessage(createDTO.getMessage());
        log.setIp(createDTO.getIp());
        log.setCreatedAt(LocalDateTime.now());
        this.save(log);
        return log;
    }
}
