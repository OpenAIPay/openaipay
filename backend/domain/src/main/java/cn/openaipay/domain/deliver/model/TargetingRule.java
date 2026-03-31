package cn.openaipay.domain.deliver.model;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定向规则模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record TargetingRule(
        /** 数据库主键ID */
        Long id,
        /** 定向规则编码。 */
        String ruleCode,
        /** 规则作用实体类型。 */
        DeliverEntityType entityType,
        /** 规则作用实体编码。 */
        String entityCode,
        /** 定向类型。 */
        DeliverTargetingType targetingType,
        /** 操作人 */
        DeliverTargetingOperator operator,
        /** 定向规则值。 */
        String targetingValue,
        /** 启用标记 */
        boolean enabled,
        /** 记录创建时间 */
        java.time.LocalDateTime createdAt,
        /** 记录更新时间 */
        java.time.LocalDateTime updatedAt
) {

    public TargetingRule {
        operator = operator == null ? DeliverTargetingOperator.IN : operator;
    }

    /**
     * 处理业务数据。
     */
    public boolean matches(DeliverContext context) {
        if (!enabled) {
            return true;
        }
        return switch (targetingType) {
            case USER_TAG -> evaluateCollection(context.userTags());
            case AUDIENCE_SEGMENT -> evaluateCollection(context.audienceSegmentCodes());
            case CHANNEL -> evaluateSingle(context.channel());
            case SCENE -> evaluateSingle(context.sceneCode());
            case CLIENT -> evaluateSingle(context.clientId());
            case TIME_RANGE -> evaluateTimeRange(context.requestTime().toLocalTime());
        };
    }

    private boolean evaluateCollection(Set<String> actualValues) {
        Set<String> expectedValues = splitValues();
        boolean matched = actualValues != null && actualValues.stream().anyMatch(expectedValues::contains);
        return switch (operator) {
            case IN, EQUALS -> matched;
            case NOT_IN, NOT_EQUALS -> !matched;
        };
    }

    private boolean evaluateSingle(String actualValue) {
        Set<String> expectedValues = splitValues();
        boolean matched = actualValue != null && expectedValues.contains(actualValue.trim());
        return switch (operator) {
            case IN, EQUALS -> matched;
            case NOT_IN, NOT_EQUALS -> !matched;
        };
    }

    private boolean evaluateTimeRange(LocalTime actualTime) {
        boolean matched = splitValues().stream().anyMatch(range -> contains(actualTime, range));
        return switch (operator) {
            case IN, EQUALS -> matched;
            case NOT_IN, NOT_EQUALS -> !matched;
        };
    }

    private boolean contains(LocalTime actualTime, String rawRange) {
        String[] parts = rawRange.split("-");
        if (parts.length != 2) {
            return false;
        }
        LocalTime start = LocalTime.parse(parts[0].trim());
        LocalTime end = LocalTime.parse(parts[1].trim());
        if (!end.isBefore(start)) {
            return !actualTime.isBefore(start) && !actualTime.isAfter(end);
        }
        return !actualTime.isBefore(start) || !actualTime.isAfter(end);
    }

    private Set<String> splitValues() {
        if (targetingValue == null || targetingValue.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(targetingValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
