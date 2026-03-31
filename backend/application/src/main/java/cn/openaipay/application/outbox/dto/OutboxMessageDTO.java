package cn.openaipay.application.outbox.dto;

import java.time.LocalDateTime;

/**
 * Outbox 消息
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record OutboxMessageDTO(
        /** 主键ID */
        Long id,
        /** 主题 */
        String topic,
        /** 消息键 */
        String messageKey,
        /** 状态 */
        String status,
        /** 当前重试次数 */
        Integer retryCount,
        /** 最大重试次数 */
        Integer maxRetryCount,
        /** 是否可重试 */
        boolean canRetry,
        /** 下次重试时间 */
        LocalDateTime nextRetryAt,
        /** 处理开始时间 */
        LocalDateTime processingStartedAt,
        /** 最近错误 */
        String lastError,
        /** 载荷预览 */
        String payloadPreview,
        /** 载荷 */
        String payload,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
