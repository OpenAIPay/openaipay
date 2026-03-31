package cn.openaipay.domain.app.model;

/**
 * App 行为埋点统计项。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorMetricItem(
        /** 统计维度键。 */
        String key,
        /** 对应数量。 */
        long count
) {
}
