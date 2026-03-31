package cn.openaipay.application.app.command;

import java.time.LocalDateTime;

/**
 * 生成 App 行为埋点报表命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record GenerateAppBehaviorEventReportCommand(
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
        /** 报表条数上限。 */
        Integer limit
) {
}
