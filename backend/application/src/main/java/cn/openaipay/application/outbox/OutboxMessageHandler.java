package cn.openaipay.application.outbox;

import cn.openaipay.domain.outbox.model.OutboxMessage;

/**
 * 标准 Outbox 消息处理器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface OutboxMessageHandler {

    /**
     * 处理主题信息。
     */
    String topic();

    void handle(OutboxMessage outboxMessage) throws Exception;
}
