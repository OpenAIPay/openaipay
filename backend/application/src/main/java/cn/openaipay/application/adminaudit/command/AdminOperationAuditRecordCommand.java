package cn.openaipay.application.adminaudit.command;

import java.time.LocalDateTime;

/**
 * 后台操作审计记录命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AdminOperationAuditRecordCommand(
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
        /** 错误摘要。 */
        String errorMessage,
        /** 执行耗时（毫秒）。 */
        Long costMs,
        /** 客户端IP。 */
        String clientIp,
        /** User-Agent。 */
        String userAgent,
        /** 创建时间。 */
        LocalDateTime createdAt
) {
}
