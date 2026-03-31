package cn.openaipay.application.outbound.dto;

/**
 * 出金概览
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record OutboundOrderOverviewDTO(
        /** 总数 */
        long totalCount,
        /** 成功数 */
        long successCount,
        /** 处理中数量 */
        long processingCount,
        /** 失败数 */
        long failedCount
) {
}
