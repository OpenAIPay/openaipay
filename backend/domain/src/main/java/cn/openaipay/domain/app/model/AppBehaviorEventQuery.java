package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;

/**
 * App 行为埋点查询条件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record AppBehaviorEventQuery(
        /** 应用编码。 */
        String appCode,
        /** 事件类型。 */
        String eventType,
        /** 事件名称。 */
        String eventName,
        /** 页面名称。 */
        String pageName,
        /** 设备ID。 */
        String deviceId,
        /** 用户ID。 */
        Long userId,
        /** 查询起始时间。 */
        LocalDateTime startAt,
        /** 查询结束时间。 */
        LocalDateTime endAt,
        /** 查询条数上限。 */
        Integer limit
) {
}
