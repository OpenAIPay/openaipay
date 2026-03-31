package cn.openaipay.application.audience.dto;

import java.time.LocalDateTime;

/**
 * 人群定义DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AudienceSegmentDTO(
        /** 人群编码 */
        String segmentCode,
        /** 人群名称 */
        String segmentName,
        /** 人群描述 */
        String description,
        /** 场景编码 */
        String sceneCode,
        /** 人群状态 */
        String status,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
