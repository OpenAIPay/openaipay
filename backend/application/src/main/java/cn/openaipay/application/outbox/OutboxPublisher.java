package cn.openaipay.application.outbox;

import cn.openaipay.domain.outbox.model.OutboxMessage;
import cn.openaipay.domain.outbox.repository.OutboxMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 标准 Outbox 发布器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class OutboxPublisher {
    /** 消息信息 */
    private final OutboxMessageRepository outboxMessageRepository;

    public OutboxPublisher(OutboxMessageRepository outboxMessageRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
    }

    /**
     * 发布条件信息。
     */
    @Transactional
    public OutboxMessage publishIfAbsent(String topic, String messageKey, String payload, int maxRetryCount) {
        return outboxMessageRepository.findByTopicAndMessageKey(topic, messageKey)
                .orElseGet(() -> outboxMessageRepository.save(
                        OutboxMessage.createPending(topic, messageKey, payload, maxRetryCount, LocalDateTime.now())
                ));
    }
}
