package cn.openaipay.application.adminrisk.dto;

import java.time.LocalDateTime;

/**
 * 风控黑名单行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminRiskBlacklistRowDTO(
        /** 所属用户ID */
        Long ownerUserId,
        /** 所属展示名称 */
        String ownerDisplayName,
        /** 所属爱付UID */
        String ownerAipayUid,
        /** 拉黑用户ID */
        Long blockedUserId,
        /** 拉黑展示名称 */
        String blockedDisplayName,
        /** 拉黑爱付UID */
        String blockedAipayUid,
        /** 原因 */
        String reason,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
