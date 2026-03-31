package cn.openaipay.application.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员摘要
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminAccountSummaryDTO(
        /** 管理员ID */
        String adminId,
        /** 用户名 */
        String username,
        /** 展示名 */
        String displayName,
        /** 状态 */
        String accountStatus,
        /** 最近登录时间 */
        LocalDateTime lastLoginAt,
        /** 角色编码 */
        List<String> roleCodes
) {
}
