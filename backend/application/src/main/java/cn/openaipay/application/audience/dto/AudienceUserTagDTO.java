package cn.openaipay.application.audience.dto;

import java.time.LocalDateTime;

/**
 * 用户标签DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceUserTagDTO(
        /** 用户ID */
        Long userId,
        /** 标签编码 */
        String tagCode,
        /** 标签值 */
        String tagValue,
        /** 数据来源 */
        String source,
        /** 标签值更新时间 */
        LocalDateTime valueUpdatedAt,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
