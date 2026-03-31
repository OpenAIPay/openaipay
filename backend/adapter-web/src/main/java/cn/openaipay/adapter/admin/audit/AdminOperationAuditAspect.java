package cn.openaipay.adapter.admin.audit;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.common.logging.ApiSceneLogSupport;
import cn.openaipay.application.adminaudit.command.AdminOperationAuditRecordCommand;
import cn.openaipay.application.adminaudit.facade.AdminOperationAuditFacade;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 后台写操作审计切面。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Aspect
@Component
public class AdminOperationAuditAspect {

    private static final Set<String> AUDIT_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AdminRequestContext adminRequestContext;
    private final AdminOperationAuditFacade adminOperationAuditFacade;

    public AdminOperationAuditAspect(AdminRequestContext adminRequestContext,
                                     AdminOperationAuditFacade adminOperationAuditFacade) {
        this.adminRequestContext = adminRequestContext;
        this.adminOperationAuditFacade = adminOperationAuditFacade;
    }

    /**
     * 拦截后台控制器写操作并记录审计日志。
     */
    @Around("execution(public * cn.openaipay.adapter.admin.web..*.*(..))")
    public Object aroundAdminWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        if (!shouldAudit(request)) {
            return joinPoint.proceed();
        }

        long startNanos = System.nanoTime();
        Object requestArg = ApiSceneLogSupport.selectRequestArg(joinPoint.getArgs());
        String requestBody = ApiSceneLogSupport.buildRequestPayload(request, requestArg);
        String resultStatus = "SUCCESS";
        String errorMessage = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            resultStatus = "FAILED";
            errorMessage = compactError(ex.getMessage());
            throw ex;
        } finally {
            adminOperationAuditFacade.record(new AdminOperationAuditRecordCommand(
                    extractTraceId(request),
                    adminRequestContext.currentAdminId(),
                    normalizeText(adminRequestContext.currentAdminUsername()),
                    normalizeMethod(request),
                    request == null ? null : truncate(request.getRequestURI(), 255),
                    request == null ? null : truncate(request.getQueryString(), 500),
                    truncate(requestBody, 4000),
                    resultStatus,
                    truncate(errorMessage, 500),
                    Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L),
                    request == null ? null : truncate(request.getRemoteAddr(), 64),
                    request == null ? null : truncate(request.getHeader("User-Agent"), 255),
                    LocalDateTime.now()
            ));
        }
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return null;
        }
        return attributes.getRequest();
    }

    private boolean shouldAudit(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String uri = normalizeText(request.getRequestURI());
        if (uri == null || !uri.startsWith("/api/admin/")) {
            return false;
        }
        String method = normalizeMethod(request);
        return AUDIT_METHODS.contains(method);
    }

    private String normalizeMethod(HttpServletRequest request) {
        String method = request == null ? null : request.getMethod();
        return method == null ? null : method.toUpperCase(Locale.ROOT);
    }

    private String extractTraceId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeader("traceId");
        }
        return truncate(normalizeText(traceId), 64);
    }

    private String compactError(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
