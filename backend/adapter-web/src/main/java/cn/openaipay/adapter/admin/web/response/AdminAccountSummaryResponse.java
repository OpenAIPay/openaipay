package cn.openaipay.adapter.admin.web.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员摘要响应模型
 *
 * 业务场景：RBAC 页面展示管理员列表，并承载为管理员分配角色时所需的基础信息。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record AdminAccountSummaryResponse(
        /** 后台ID */
        String adminId,
        /** 用户名 */
        String username,
        /** 展示名称 */
        String displayName,
        /** 业务状态 */
        String accountStatus,
        /** 最近一次登录时间 */
        LocalDateTime lastLoginAt,
        /** 角色信息 */
        List<String> roleCodes
) {
}
