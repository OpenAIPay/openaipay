package cn.openaipay.domain.audience.model;

import java.time.LocalDateTime;

/**
 * 用户标签快照
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceUserTagSnapshot(
        /** 主键ID */
        Long id,
        /** 用户ID */
        Long userId,
        /** 标签编码 */
        String tagCode,
        /** 标签值 */
        String tagValue,
        /** 来源 */
        String source,
        /** 标签值更新时间 */
        LocalDateTime valueUpdatedAt,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
    public AudienceUserTagSnapshot {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        tagCode = trimRequired(tagCode, "tagCode");
        tagValue = trimRequired(tagValue, "tagValue");
        source = trimNullable(source);
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
