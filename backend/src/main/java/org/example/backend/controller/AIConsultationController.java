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
        try {
            Long userId = tokenUtil.resolveUserIdFromToken(
                    tokenUtil.extractToken(authHeader, tokenParam));

            StringBuilder sb = new StringBuilder();
            sb.append("问: ").append(trim(request.getMessage(), 500));
            if (response != null && response.getReply() != null) {
                sb.append(" | 答: ").append(trim(response.getReply(), 500));
            }

            AuditLogCreateDTO dto = new AuditLogCreateDTO();
            dto.setAction("create");
            dto.setResourceType("ai_consultation");
            dto.setMessage(sb.toString());
            dto.setIp(httpRequest != null ? httpRequest.getRemoteAddr() : null);
            auditLogService.recordLog(userId, dto);
        } catch (Exception ignored) {
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
