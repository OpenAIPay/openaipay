package cn.openaipay.application.outbox;

import cn.openaipay.domain.outbox.model.OutboxMessage;
import cn.openaipay.domain.outbox.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 标准 Outbox 消息分发服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Service
public class OutboxDispatchService {
    /** LOG信息 */
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatchService.class);

    /** 消息信息 */
    private final OutboxMessageRepository outboxMessageRepository;
    /** MAP信息 */
    private final Map<String, OutboxMessageHandler> handlerMap;
    /** batchsize信息 */
    private final int batchSize;
    /** 处理信息 */
    private final Duration processingTimeout;
    /** 重试信息 */
    private final Duration retryBackoff;

    public OutboxDispatchService(OutboxMessageRepository outboxMessageRepository,
                                 List<OutboxMessageHandler> handlers,
                                 @Value("${aipay.async-message.batch-size:8}") int batchSize,
                                 @Value("${aipay.async-message.processing-timeout-seconds:120}") long processingTimeoutSeconds,
                                 @Value("${aipay.async-message.retry-backoff-millis:1500}") long retryBackoffMillis) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.handlerMap = buildHandlerMap(handlers);
        this.batchSize = Math.max(1, batchSize);
        this.processingTimeout = Duration.ofSeconds(Math.max(1L, processingTimeoutSeconds));
        this.retryBackoff = Duration.ofMillis(Math.max(100L, retryBackoffMillis));
        String scene = "Outbox初始化";
        String request = "topics=" + new TreeSet<>(this.handlerMap.keySet());
        log.info("[{}]入参：{}", scene, request);
    }

    /**
     * 分发批量信息。
     */
    public int dispatchBatch() {
        List<OutboxMessage> outboxMessages = outboxMessageRepository.claimNextBatch(batchSize, LocalDateTime.now(), processingTimeout);
        if (outboxMessages.isEmpty()) {
            return 0;
        }
        int handledCount = 0;
        for (OutboxMessage outboxMessage : outboxMessages) {
            handledCount++;
            process(outboxMessage);
        }
        return handledCount;
    }

    private void process(OutboxMessage outboxMessage) {
        OutboxMessageHandler handler = handlerMap.get(outboxMessage.getTopic());
        if (handler == null) {
            log.warn(
                    "outbox handler missing topic={}, registeredTopics={}",
                    outboxMessage.getTopic(),
                    new TreeSet<>(handlerMap.keySet())
            );
            outboxMessage.markDead("no outbox handler for topic: " + outboxMessage.getTopic(), LocalDateTime.now());
            outboxMessageRepository.save(outboxMessage);
            return;
        }

        try {
            handler.handle(outboxMessage);
            outboxMessage.markSucceeded(LocalDateTime.now());
            outboxMessageRepository.save(outboxMessage);
        } catch (Exception ex) {
            String errorMessage = compactError(ex.getMessage());
            if (outboxMessage.canRetry()) {
                LocalDateTime nextRetryAt = LocalDateTime.now().plus(retryBackoff);
                outboxMessage.markRetry(errorMessage, nextRetryAt, LocalDateTime.now());
                outboxMessageRepository.save(outboxMessage);
                log.warn(
                        "outbox message retry scheduled topic={}, key={}, retryCount={}, message={}",
                        outboxMessage.getTopic(),
                        outboxMessage.getMessageKey(),
                        outboxMessage.getRetryCount(),
                        errorMessage
                );
                return;
            }

            outboxMessage.markDead(errorMessage, LocalDateTime.now());
            outboxMessageRepository.save(outboxMessage);
            log.warn(
                    "outbox message moved to dead topic={}, key={}, retryCount={}, message={}",
                    outboxMessage.getTopic(),
                    outboxMessage.getMessageKey(),
                    outboxMessage.getRetryCount(),
                    errorMessage
            );
        }
    }

    private Map<String, OutboxMessageHandler> buildHandlerMap(List<OutboxMessageHandler> handlers) {
        Map<String, OutboxMessageHandler> result = new LinkedHashMap<>();
        for (OutboxMessageHandler handler : handlers) {
            String topic = normalizeTopic(handler.topic(), handler.getClass().getName());
            if (result.containsKey(topic)) {
                throw new IllegalStateException("duplicate outbox handler topic: " + topic);
            }
            result.put(topic, handler);
        }
        return Map.copyOf(result);
    }

    private String normalizeTopic(String topic, String handlerName) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("handler topic must not be blank, handler=" + handlerName);
        }
        return topic.trim();
    }

    private String compactError(String raw) {
        if (raw == null || raw.isBlank()) {
            return "unknown error";
        }
        String normalized = raw.trim();
        return normalized.length() <= 180 ? normalized : normalized.substring(0, 180);
    }
}
