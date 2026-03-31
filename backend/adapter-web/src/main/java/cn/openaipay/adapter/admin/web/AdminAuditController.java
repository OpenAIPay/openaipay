package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.adminaudit.dto.AdminOperationAuditRowDTO;
import cn.openaipay.application.adminaudit.facade.AdminOperationAuditFacade;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台操作审计查询控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@RestController
@RequestMapping("/api/admin/audits")
public class AdminAuditController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    private final AdminOperationAuditFacade adminOperationAuditFacade;

    public AdminAuditController(AdminOperationAuditFacade adminOperationAuditFacade) {
        this.adminOperationAuditFacade = adminOperationAuditFacade;
    }

    /**
     * 查询后台操作审计日志。
     */
    @GetMapping
    @RequireAdminPermission("audit.log.view")
    public ApiResponse<List<AdminAuditRow>> list(
            @RequestParam(value = "adminId", required = false) Long adminId,
            @RequestParam(value = "requestMethod", required = false) String requestMethod,
            @RequestParam(value = "requestPath", required = false) String requestPath,
            @RequestParam(value = "resultStatus", required = false) String resultStatus,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime to,
            @RequestParam(value = "limit", required = false) Integer limit) {
        List<AdminAuditRow> rows = adminOperationAuditFacade.list(
                adminId,
                requestMethod,
                requestPath,
                resultStatus,
                from,
                to,
                normalizeLimit(limit)
        ).stream().map(this::toRow).toList();
        return ApiResponse.success(rows);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private AdminAuditRow toRow(AdminOperationAuditRowDTO entity) {
        return new AdminAuditRow(
                entity.id(),
                entity.traceId(),
                entity.adminId(),
                entity.adminUsername(),
                entity.requestMethod(),
                entity.requestPath(),
                entity.requestQuery(),
                entity.requestBody(),
                entity.resultStatus(),
                entity.errorMessage(),
                entity.costMs(),
                entity.clientIp(),
                entity.userAgent(),
                entity.createdAt()
        );
    }

    /**
     * 审计日志行。
     */
    public record AdminAuditRow(
            /** 主键ID。 */
            Long id,
            /** 追踪号。 */
            String traceId,
            /** 管理员ID。 */
            Long adminId,
            /** 管理员账号。 */
            String adminUsername,
            /** 请求方法。 */
            String requestMethod,
            /** 请求路径。 */
            String requestPath,
            /** 请求query。 */
            String requestQuery,
            /** 请求体摘要。 */
            String requestBody,
            /** 执行结果。 */
            String resultStatus,
            /** 错误信息。 */
            String errorMessage,
            /** 执行耗时。 */
            Long costMs,
            /** 客户端IP。 */
            String clientIp,
            /** User-Agent。 */
            String userAgent,
            /** 创建时间。 */
            LocalDateTime createdAt
    ) {
    }
}
