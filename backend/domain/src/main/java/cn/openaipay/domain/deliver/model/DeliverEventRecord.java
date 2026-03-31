package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;

/**
 * 投放事件记录模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverEventRecord(
        /** 数据库主键ID */
        Long id,
        /** 客户端ID。 */
        String clientId,
        /** 用户ID */
        Long userId,
        /** 事件关联实体类型。 */
        DeliverEntityType entityType,
        /** 事件关联实体编码。 */
        String entityCode,
        /** 展位编码。 */
        String positionCode,
        /** 投放单元编码。 */
        String unitCode,
        /** 创意编码。 */
        String creativeCode,
        /** 事件类型。 */
        DeliverEventType eventType,
        /** 场景编码。 */
        String sceneCode,
        /** 渠道编码。 */
        String channel,
        /** 事件发生时间。 */
        LocalDateTime eventTime,
        /** 记录创建时间 */
        LocalDateTime createdAt
) {
}
