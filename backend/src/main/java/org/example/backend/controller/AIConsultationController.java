package org.example.backend.controller;

import org.example.backend.dto.ConsultationRequest;
import org.example.backend.dto.ConsultationResponse;
import org.example.backend.service.AIConsultationService;
import org.example.backend.service.AuditLogService;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import static com.baomidou.mybatisplus.extension.ddl.DdlScriptErrorHandler.PrintlnLogErrorHandler.log;

@RestController
@RequestMapping("/api/consultation")
@CrossOrigin(originPatterns = "*") // Allow CORS for development
public class AIConsultationController {

    @Autowired
    private AIConsultationService aiConsultationService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private TokenUtil tokenUtil;

    @PostMapping("/chat")
    public ResponseEntity<ConsultationResponse> chat(
            @RequestBody ConsultationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "token", required = false) String tokenParam,
            HttpServletRequest httpRequest) {
        ConsultationResponse response = aiConsultationService.chat(request);

        // 审计：记录问答
        Long userId = null;
        try {
            System.out.println("authHeader = " + authHeader);
            System.out.println("tokenParam = " + tokenParam);

            String token = tokenUtil.extractToken(authHeader, tokenParam);
            System.out.println("resolved token = " + token);
            if (token != null) {
                userId = tokenUtil.resolveUserIdFromToken(token);
            }
            System.out.println("resolved userId = " + userId);
        } catch (Exception e) {
            // 只记录警告，不吞掉逻辑
            log.warn("解析 userId 失败");
        }

        // 构建日志内容
        StringBuilder sb = new StringBuilder();
        sb.append("问: ").append(trim(request.getMessage(), 500));
        if (response != null && response.getReply() != null) {
            sb.append(" | 答: ").append(trim(response.getReply(), 500));
        }

        // 写审计日志（始终执行）
        try {
            AuditLogCreateDTO dto = new AuditLogCreateDTO();
            dto.setAction("create");
            dto.setResourceType("ai_consultation");
            dto.setMessage(sb.toString());
            dto.setIp(httpRequest != null ? httpRequest.getRemoteAddr() : null);
            auditLogService.recordLog(userId, dto);
        } catch (Exception e) {
            log.warn("写入 AI 问诊审计日志失败");
        }

        return ResponseEntity.ok(response);
    }

    private String trim(String text, int max) {
        if (text == null)
            return null;
        if (text.length() <= max)
            return text;
        return text.substring(0, max) + "...";
    }
}
