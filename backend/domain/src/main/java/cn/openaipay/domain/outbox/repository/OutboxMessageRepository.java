package cn.openaipay.domain.outbox.repository;

import cn.openaipay.domain.outbox.model.OutboxMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 标准 Outbox 消息仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface OutboxMessageRepository {

    /**
     * 保存业务数据。
     */
    OutboxMessage save(OutboxMessage outboxMessage);

    /**
     * 按主题与消息KEY查找记录。
     */
    Optional<OutboxMessage> findByTopicAndMessageKey(String topic, String messageKey);

    /**
     * 按主键查询消息。
     */
    Optional<OutboxMessage> findById(Long id);

    /**
     * 处理批量信息。
     */
    List<OutboxMessage> claimNextBatch(int batchSize, LocalDateTime now, Duration processingTimeout);

    /**
     * 重新入队单条死信消息。
     */
    boolean requeueDeadLetter(Long id, LocalDateTime nextRetryAt, String operator);

    /**
     * 批量重新入队死信消息。
     */
    int requeueDeadLetters(String topic, int limit, LocalDateTime nextRetryAt, String operator);
}
