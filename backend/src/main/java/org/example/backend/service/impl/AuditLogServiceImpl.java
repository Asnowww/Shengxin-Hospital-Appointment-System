package org.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.dto.AuditLogQueryDTO;
import org.example.backend.dto.PageResult;
import org.example.backend.mapper.AuditLogMapper;
import org.example.backend.pojo.AuditLog;
import org.example.backend.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    @Resource
    private AuditLogMapper auditLogMapper;

//    @Override
//    public PageResult<AuditLog> queryLogs(AuditLogQueryDTO queryDTO) {
//        Page<AuditLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
//
//        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
//        if (queryDTO.getUserId() != null) {
//            wrapper.eq(AuditLog::getUserId, queryDTO.getUserId());
//        }
//        if (StringUtils.isNotBlank(queryDTO.getAction())) {
//            wrapper.eq(AuditLog::getAction, queryDTO.getAction());
//        }
//        if (StringUtils.isNotBlank(queryDTO.getResourceType())) {
//            wrapper.eq(AuditLog::getResourceType, queryDTO.getResourceType());
//        }
//        if (queryDTO.getResourceId() != null) {
//            wrapper.eq(AuditLog::getResourceId, queryDTO.getResourceId());
//        }
//        LocalDateTime startTime = queryDTO.getStartTime();
//        LocalDateTime endTime = queryDTO.getEndTime();
//        if (startTime != null) {
//            wrapper.ge(AuditLog::getCreatedAt, startTime);
//        }
//        if (endTime != null) {
//            wrapper.le(AuditLog::getCreatedAt, endTime);
//        }
//        wrapper.orderByDesc(AuditLog::getCreatedAt);
//
//        Page<AuditLog> result = auditLogMapper.selectPage(page, wrapper);
//
//        //Page<AuditLog> result = this.page(page, wrapper);
//        return new PageResult<>(
//                result.getTotal(),
//                result.getRecords(),
//                (int) result.getCurrent(),
//                (int) result.getSize());
//    }
@Override
public PageResult<AuditLog> queryLogs(AuditLogQueryDTO dto) {

    int pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
    int pageSize = dto.getPageSize() == null ? 20 : dto.getPageSize();
    int offset = (pageNum - 1) * pageSize;

    List<AuditLog> list = auditLogMapper.selectPageList(
            dto.getUserId(),
            dto.getAction(),
            dto.getResourceType(),
            dto.getResourceId(),
            dto.getStartTime(),
            dto.getEndTime(),
            offset,
            pageSize
    );

    long total = auditLogMapper.countPageList(
            dto.getUserId(),
            dto.getAction(),
            dto.getResourceType(),
            dto.getResourceId(),
            dto.getStartTime(),
            dto.getEndTime()
    );

    return new PageResult<>(total, list, pageNum, pageSize);
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
