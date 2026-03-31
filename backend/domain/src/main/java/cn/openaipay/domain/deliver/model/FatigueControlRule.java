package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;

/**
 * 疲劳度控制规则模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record FatigueControlRule(
        /** 数据库主键ID */
        Long id,
        /** 疲劳度规则编码。 */
        String fatigueCode,
        /** 规则名称。 */
        String ruleName,
        /** 规则作用实体类型。 */
        DeliverEntityType entityType,
        /** 规则作用实体编码。 */
        String entityCode,
        /** 统计事件类型。 */
        DeliverEventType eventType,
        /** 统计时间窗口（分钟）。 */
        Integer timeWindowMinutes,
        /** 窗口内最大触发次数。 */
        Integer maxCount,
        /** 启用标记 */
        boolean enabled,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {

    public FatigueControlRule {
        eventType = eventType == null ? DeliverEventType.DISPLAY : eventType;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isBlocked(long recentCount) {
        int threshold = maxCount == null ? 0 : maxCount;
        return threshold > 0 && recentCount >= threshold;
    }

    /**
     * 解析业务数据。
     */
    public int resolveWindowMinutes() {
        return timeWindowMinutes == null || timeWindowMinutes <= 0 ? 60 : timeWindowMinutes;
    }
}
