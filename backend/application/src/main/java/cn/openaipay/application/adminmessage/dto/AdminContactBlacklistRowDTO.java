package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;

/**
 * 黑名单行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminContactBlacklistRowDTO(
        /** 所属用户ID */
        Long ownerUserId,
        /** 所属展示名称 */
        String ownerDisplayName,
        /** 拉黑用户ID */
        Long blockedUserId,
        /** 拉黑展示名称 */
        String blockedDisplayName,
        /** 原因 */
        String reason,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
