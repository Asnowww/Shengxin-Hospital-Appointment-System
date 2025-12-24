package org.example.backend.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.dto.AuditLogQueryDTO;
import org.example.backend.dto.PageResult;
import org.example.backend.dto.Result;
import org.example.backend.pojo.AuditLog;
import org.example.backend.service.AuditLogService;
import org.example.backend.util.TokenUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志接口
 */
@RestController
@RequestMapping("/api/logs")
public class AuditLogController {

    @Resource
    private AuditLogService auditLogService;

    @Resource
    private TokenUtil tokenUtil;

    /**
     * 分页查询日志，支持条件筛选
     */
    @GetMapping
    public Result<PageResult<AuditLog>> listLogs(AuditLogQueryDTO queryDTO) {
        return Result.success(auditLogService.queryLogs(queryDTO));
    }

    /**
     * 查看单条日志
     */
    @GetMapping("/{id}")
    public Result<AuditLog> getLog(@PathVariable Long id) {
        AuditLog log = auditLogService.getById(id);
        if (log == null) {
            return Result.error("日志不存在");
        }
        return Result.success(log);
    }

    /**
     * 新增日志（默认根据 token 填充 userId）
     */
    @PostMapping
    public Result<AuditLog> createLog(
            @RequestBody AuditLogCreateDTO createDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest request) {
        String token = tokenUtil.extractToken(authHeader, tokenParam);
        Long userId = tokenUtil.resolveUserIdFromToken(token);
        if (userId == null) {
            return Result.error("token 无效或已过期");
        }

        if (StringUtils.isBlank(createDTO.getIp())) {
            createDTO.setIp(request.getRemoteAddr());
        }

        AuditLog log = auditLogService.recordLog(userId, createDTO);
        return Result.success("记录成功", log);
    }
}
