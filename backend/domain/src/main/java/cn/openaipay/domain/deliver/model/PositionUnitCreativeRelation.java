package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;

/**
 * 展位-单元-创意关系模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record PositionUnitCreativeRelation(
        /** 数据库主键ID */
        Long id,
        /** 展位ID。 */
        Long positionId,
        /** 投放单元ID。 */
        Long unitId,
        /** 创意ID。 */
        Long creativeId,
        /** 展示顺序。 */
        Integer displayOrder,
        /** 兜底标记 */
        boolean fallback,
        /** 启用标记 */
        boolean enabled,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
