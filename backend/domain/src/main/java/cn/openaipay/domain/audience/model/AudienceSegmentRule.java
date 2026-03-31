package cn.openaipay.domain.audience.model;

import java.time.LocalDateTime;

/**
 * 人群规则
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceSegmentRule(
        /** 主键ID */
        Long id,
        /** 规则编码 */
        String ruleCode,
        /** 人群编码 */
        String segmentCode,
        /** 标签编码 */
        String tagCode,
        /** 操作符 */
        AudienceRuleOperator operator,
        /** 目标值 */
        String targetValue,
        /** 规则归属 */
        AudienceRuleRelation relation,
        /** 是否启用 */
        boolean enabled,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
    public AudienceSegmentRule {
        ruleCode = trimRequired(ruleCode, "ruleCode");
        segmentCode = trimRequired(segmentCode, "segmentCode");
        tagCode = trimRequired(tagCode, "tagCode");
        operator = operator == null ? AudienceRuleOperator.EQ : operator;
        relation = relation == null ? AudienceRuleRelation.INCLUDE : relation;
        targetValue = trimNullable(targetValue);
    }

    private static String trimRequired(String value, String field) {
        String normalized = trimNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private static String trimNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
