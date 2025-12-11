package org.example.backend.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.backend.dto.AuditLogCreateDTO;
import org.example.backend.pojo.AuditLog;
import org.example.backend.service.AuditLogService;
import org.example.backend.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * 审计日志切面：拦截所有 RestController 的请求并记录
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final TokenUtil tokenUtil;

    @Around("@within(org.springframework.web.bind.annotation.RestController) " +
            "|| @annotation(org.springframework.web.bind.annotation.RestController) " +
            "|| @within(org.springframework.stereotype.Controller) " +
            "|| @annotation(org.springframework.stereotype.Controller)")
    public Object recordAudit(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            // 非 Web 场景不记录
            return pjp.proceed();
        }

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Audit auditAnno = resolveAuditAnnotation(method);

        Object result = null;
        Throwable throwable = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            // 成功时记录；如需记录失败，可将条件改为始终记录并在 message 中标注异常
            if (throwable == null) {
                try {
                    writeAuditLog(request, method, auditAnno);
                } catch (Exception e) {
                    log.warn("写入审计日志失败: {}", e.getMessage(), e);
                }
            }
        }
    }

    private void writeAuditLog(HttpServletRequest request, Method method, Audit auditAnno) {
        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        String action = chooseAction(auditAnno, httpMethod, uri);
        String resourceType = chooseResourceType(auditAnno, uri);
        Long resourceId = guessResourceId(request);

        AuditLogCreateDTO dto = new AuditLogCreateDTO();
        dto.setAction(action);
        dto.setResourceType(resourceType);
        dto.setResourceId(resourceId);

        String msg = StringUtils.defaultIfBlank(
                auditAnno != null ? auditAnno.message() : null,
                httpMethod + " " + uriWithQuery(request));
        dto.setMessage(msg);
        dto.setIp(request.getRemoteAddr());

        Long userId = extractUserId(request);

        AuditLog saved = auditLogService.recordLog(userId, dto);
        log.debug("审计日志 recorded, id={}", saved.getLogId());
    }

    private String chooseAction(Audit auditAnno, String httpMethod, String uri) {
        if (auditAnno != null && StringUtils.isNotBlank(auditAnno.action())) {
            return auditAnno.action();
        }

        // 特殊路径优先
        if (uri.contains("/login")) {
            return "login";
        }
        if (uri.contains("/logout")) {
            return "logout";
        }

        // 按 HTTP 方法推断；GET 也记为 update（表枚举限制）
        return switch (httpMethod.toUpperCase()) {
            case "POST" -> "create";
            case "PUT", "PATCH" -> "update";
            case "DELETE" -> "delete";
            default -> "update";
        };
    }

    private String chooseResourceType(Audit auditAnno, String uri) {
        if (auditAnno != null && StringUtils.isNotBlank(auditAnno.resourceType())) {
            return auditAnno.resourceType();
        }

        // 取 /api 后第一个片段作为资源，去掉尾部 s
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("api".equalsIgnoreCase(parts[i]) && i + 1 < parts.length) {
                String candidate = parts[i + 1];
                if (candidate.endsWith("s") && candidate.length() > 1) {
                    candidate = candidate.substring(0, candidate.length() - 1);
                }
                return candidate.toLowerCase();
            }
        }
        return "system";
    }

    private Long guessResourceId(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (attr instanceof Map<?, ?> vars) {
            Optional<Long> firstId = vars.values().stream()
                    .map(Object::toString)
                    .filter(StringUtils::isNotBlank)
                    .map(val -> {
                        try {
                            return Long.parseLong(val);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(v -> v != null)
                    .findFirst();
            return firstId.orElse(null);
        }
        return null;
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = tokenUtil.extractToken(
                request.getHeader("Authorization"),
                request.getParameter("token"));
        return tokenUtil.resolveUserIdFromToken(token);
    }

    private String uriWithQuery(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String qs = request.getQueryString();
        if (StringUtils.isBlank(qs)) {
            return uri;
        }
        return uri + "?" + qs;
    }

    private Audit resolveAuditAnnotation(Method method) {
        Audit methodAnno = method.getAnnotation(Audit.class);
        if (methodAnno != null)
            return methodAnno;
        return method.getDeclaringClass().getAnnotation(Audit.class);
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null)
            return null;
        return (HttpServletRequest) attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);
    }
}
