package cn.openaipay.application.deliver.command;

import cn.openaipay.domain.deliver.model.DeliverEventType;
import java.time.LocalDateTime;

/**
 * 投放事件回传命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverEventCommand(
        /** 业务ID */
        String clientId,
        /** 用户ID */
        Long userId,
        /** 场景编码 */
        String sceneCode,
        /** 渠道信息 */
        String channel,
        /** 位置编码 */
        String positionCode,
        /** 单元编码 */
        String unitCode,
        /** 业务编码 */
        String creativeCode,
        /** 事件类型 */
        DeliverEventType eventType,
        /** 事件时间 */
        LocalDateTime eventTime
) {

    public DeliverEventCommand {
        clientId = normalize(clientId);
        sceneCode = normalize(sceneCode);
        channel = normalize(channel);
        positionCode = normalize(positionCode);
        unitCode = normalize(unitCode);
        creativeCode = normalize(creativeCode);
        eventType = eventType == null ? DeliverEventType.CLICK : eventType;
        eventTime = eventTime == null ? LocalDateTime.now() : eventTime;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
