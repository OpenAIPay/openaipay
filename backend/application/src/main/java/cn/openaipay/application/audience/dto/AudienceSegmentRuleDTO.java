package cn.openaipay.application.audience.dto;

import java.time.LocalDateTime;

/**
 * 人群规则DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceSegmentRuleDTO(
        /** 规则编码 */
        String ruleCode,
        /** 人群编码 */
        String segmentCode,
        /** 标签编码 */
        String tagCode,
        /** 操作符 */
        String operator,
        /** 目标值 */
        String targetValue,
        /** 规则归属 */
        String relation,
        /** 是否启用 */
        boolean enabled,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
