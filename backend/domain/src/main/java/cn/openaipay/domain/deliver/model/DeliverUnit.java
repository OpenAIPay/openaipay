package cn.openaipay.domain.deliver.model;

import java.time.LocalDateTime;

/**
 * 投放单元模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverUnit(
        /** 数据库主键ID */
        Long id,
        /** 投放单元编码。 */
        String unitCode,
        /** 投放单元名称。 */
        String unitName,
        /** 优先级。 */
        Integer priority,
        /** 状态编码 */
        DeliverPublishStatus status,
        /** 生效开始时间 */
        LocalDateTime activeFrom,
        /** 生效结束时间 */
        LocalDateTime activeTo,
        /** 备注 */
        String memo,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {

    /**
     * 判断是否AT信息。
     */
    public boolean isPublishedAt(LocalDateTime now) {
        if (status != DeliverPublishStatus.PUBLISHED) {
            return false;
        }
        LocalDateTime targetTime = now == null ? LocalDateTime.now() : now;
        if (activeFrom != null && activeFrom.isAfter(targetTime)) {
            return false;
        }
        return activeTo == null || !activeTo.isBefore(targetTime);
    }
}
