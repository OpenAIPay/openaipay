package cn.openaipay.domain.audience.model;

import java.time.LocalDateTime;

/**
 * 人群定义
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceSegment(
        /** 主键ID */
        Long id,
        /** 人群编码 */
        String segmentCode,
        /** 人群名称 */
        String segmentName,
        /** 人群描述 */
        String description,
        /** 场景编码 */
        String sceneCode,
        /** 人群状态 */
        AudienceSegmentStatus status,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
    public AudienceSegment {
        segmentCode = trimRequired(segmentCode, "segmentCode");
        segmentName = trimRequired(segmentName, "segmentName");
        description = trimNullable(description);
        sceneCode = trimNullable(sceneCode);
        status = status == null ? AudienceSegmentStatus.DRAFT : status;
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
