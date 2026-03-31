package cn.openaipay.application.admin.dto;

import java.time.LocalDateTime;
/**
 * 后台管理资料数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminProfileDTO(
        /** 后台ID */
        String adminId,
        /** 用户名 */
        String username,
        /** 展示名称 */
        String displayName,
        /** 业务状态 */
        String accountStatus,
        /** 最近一次登录时间 */
        LocalDateTime lastLoginAt
) {
}
