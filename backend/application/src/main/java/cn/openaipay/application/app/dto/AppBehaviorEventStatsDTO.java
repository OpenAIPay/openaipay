package cn.openaipay.application.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * App 行为埋点统计 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorEventStatsDTO(
        /** 埋点总数。 */
        long totalCount,
        /** 去重设备数。 */
        long uniqueDeviceCount,
        /** 去重用户数。 */
        long uniqueUserCount,
        /** 成功事件数。 */
        long successCount,
        /** 失败事件数。 */
        long failureCount,
        /** 平均耗时（毫秒）。 */
        BigDecimal avgDurationMs,
        /** 最早事件时间。 */
        LocalDateTime firstOccurredAt,
        /** 最近事件时间。 */
        LocalDateTime lastOccurredAt,
        /** 事件类型分布。 */
        List<AppBehaviorMetricItemDTO> eventTypeDistribution,
        /** 高频事件分布。 */
        List<AppBehaviorMetricItemDTO> topEventDistribution
) {
}
