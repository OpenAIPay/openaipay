package cn.openaipay.application.outbox.dto;

/**
 * Outbox 概览
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record OutboxOverviewDTO(
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
        long retriedCount,
        /** 待重试数量 */
        long retryPendingCount,
        /** 可分发数量 */
        long readyDispatchCount,
        /** 超时处理中数量 */
        long staleProcessingCount
) {
}
