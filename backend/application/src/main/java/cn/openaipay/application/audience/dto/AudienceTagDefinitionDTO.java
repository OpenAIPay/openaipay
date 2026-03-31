package cn.openaipay.application.audience.dto;

import java.time.LocalDateTime;

/**
 * 标签定义DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceTagDefinitionDTO(
        /** 标签编码 */
        String tagCode,
        /** 标签名称 */
        String tagName,
        /** 标签类型 */
        String tagType,
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
}
