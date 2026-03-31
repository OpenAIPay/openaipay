package cn.openaipay.application.inbound.dto;

/**
 * 入金概览
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record InboundOrderOverviewDTO(
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
