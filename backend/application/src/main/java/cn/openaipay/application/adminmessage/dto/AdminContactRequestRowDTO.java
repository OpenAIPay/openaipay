package cn.openaipay.application.adminmessage.dto;

import java.time.LocalDateTime;

/**
 * 好友申请行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminContactRequestRowDTO(
        /** 请求号 */
        String requestNo,
        /** 申请方用户ID */
        Long requesterUserId,
        /** 申请方展示名称 */
        String requesterDisplayName,
        /** 目标用户ID */
        Long targetUserId,
        /** 目标展示名称 */
        String targetDisplayName,
        /** 申请说明 */
        String applyMessage,
        /** 状态 */
        String status,
        /** 处理人用户ID */
        Long handledByUserId,
        /** 处理时间 */
        LocalDateTime handledAt,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
