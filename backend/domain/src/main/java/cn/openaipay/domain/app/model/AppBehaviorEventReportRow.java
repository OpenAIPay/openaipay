package cn.openaipay.domain.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * App 行为埋点报表行。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorEventReportRow(
        /** 统计日期。 */
        LocalDate statDate,
        /** 事件类型。 */
        String eventType,
        /** 事件名称。 */
        String eventName,
        /** 事件总数。 */
        long totalCount,
        /** 成功事件数。 */
        long successCount,
        /** 失败事件数。 */
        long failureCount,
        /** 去重设备数。 */
        long deviceCount,
        /** 去重用户数。 */
        long userCount,
        /** 平均耗时（毫秒）。 */
        BigDecimal avgDurationMs,
        /** 最大耗时（毫秒）。 */
        Long maxDurationMs
) {
}
