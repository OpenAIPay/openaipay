package cn.openaipay.application.app.dto;

/**
 * App 行为埋点统计项 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorMetricItemDTO(
        /** 统计维度键。 */
        String key,
        /** 数量。 */
        long count
) {
}
