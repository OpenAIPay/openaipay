package cn.openaipay.application.outbox.dto;

/**
 * Outbox 主题分布
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record OutboxTopicDistributionDTO(
        /** 主题 */
        String topic,
        /** 总数 */
        long totalCount,
        /** 待处理数量 */
        long pendingCount,
        /** 处理中数量 */
        long processingCount,
        /** 成功数量 */
        long succeededCount,
        /** 死信数量 */
        long deadCount,
        /** 已重试数量 */
        long retriedCount
) {
}
