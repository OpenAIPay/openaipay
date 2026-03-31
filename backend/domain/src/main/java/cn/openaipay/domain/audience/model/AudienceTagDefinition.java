package cn.openaipay.domain.audience.model;

import java.time.LocalDateTime;

/**
 * 人群标签定义
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceTagDefinition(
        /** 主键ID */
        Long id,
        /** 标签编码 */
        String tagCode,
        /** 标签名称 */
        String tagName,
        /** 标签类型 */
        AudienceTagType tagType,
        /** 标签值域 */
        String valueScope,
        /** 标签描述 */
        String description,
        /** 是否启用 */
        boolean enabled,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
    public AudienceTagDefinition {
        tagCode = trimRequired(tagCode, "tagCode");
        tagName = trimRequired(tagName, "tagName");
        tagType = tagType == null ? AudienceTagType.ENUM : tagType;
        valueScope = trimNullable(valueScope);
        description = trimNullable(description);
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
