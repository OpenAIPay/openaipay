package cn.openaipay.application.adminrisk.dto;

/**
 * 风控概览
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminRiskOverviewDTO(
        /** 总用户数量 */
        long totalUserCount,
        /** L0数量 */
        long l0Count,
        /** L1数量 */
        long l1Count,
        /** L2数量 */
        long l2Count,
        /** L3数量 */
        long l3Count,
        /** 低风险数量 */
        long lowRiskCount,
        /** 中风险数量 */
        long mediumRiskCount,
        /** 高风险数量 */
        long highRiskCount,
        /** 黑名单数量 */
        long blacklistCount
) {
}
