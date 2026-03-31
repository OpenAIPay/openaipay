package cn.openaipay.application.outbox.service.impl;

import cn.openaipay.application.outbox.dto.OutboxMessageDTO;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.dto.OutboxTopicDistributionDTO;
import cn.openaipay.application.outbox.port.OutboxReadPort;
import cn.openaipay.application.outbox.service.OutboxMonitorService;
import cn.openaipay.domain.outbox.repository.OutboxMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息投递中心服务实现（Outbox）
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class OutboxMonitorServiceImpl implements OutboxMonitorService {

    private static final int DEFAULT_REQUEUE_LIMIT = 20;

    private final OutboxReadPort outboxReadPort;
    private final OutboxMessageRepository outboxMessageRepository;
    private final long processingTimeoutSeconds;

    public OutboxMonitorServiceImpl(OutboxReadPort outboxReadPort,
                                    OutboxMessageRepository outboxMessageRepository,
                                    @Value("${aipay.async-message.processing-timeout-seconds:120}") long processingTimeoutSeconds) {
        this.outboxReadPort = outboxReadPort;
        this.outboxMessageRepository = outboxMessageRepository;
        this.processingTimeoutSeconds = Math.max(1L, processingTimeoutSeconds);
    }

    @Override
    @Transactional(readOnly = true)
    public OutboxOverviewDTO getOverview() {
        return outboxReadPort.getOverview(processingTimeoutSeconds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxTopicDistributionDTO> listTopicDistribution(Integer limit) {
        return outboxReadPort.listTopicDistribution(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxMessageDTO> listMessages(String topic,
                                               String status,
                                               String keyword,
                                               Boolean onlyRetried,
                                               Integer limit,
                                               Boolean includePayload) {
        return outboxReadPort.listMessages(topic, status, keyword, onlyRetried, limit, includePayload);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxMessageDTO> listDeadLetters(String topic,
                                                  String keyword,
                                                  Integer limit,
                                                  Boolean includePayload) {
        return outboxReadPort.listMessages(topic, "DEAD", keyword, null, limit, includePayload);
    }

    @Override
    @Transactional(readOnly = true)
    public OutboxMessageDTO getMessage(Long id) {
        return outboxReadPort.getMessage(id);
    }

    @Override
    @Transactional
    public boolean requeueDeadLetter(Long id, LocalDateTime nextRetryAt, String operator) {
        return outboxMessageRepository.requeueDeadLetter(requirePositive(id, "id"), nextRetryAt, operator);
    }

    @Override
    @Transactional
    public int requeueDeadLetters(String topic, Integer limit, LocalDateTime nextRetryAt, String operator) {
        int normalizedLimit = limit == null ? DEFAULT_REQUEUE_LIMIT : Math.max(1, Math.min(limit, 200));
        return outboxMessageRepository.requeueDeadLetters(normalizeTopic(topic), normalizedLimit, nextRetryAt, operator);
    }

    private Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
        return value;
    }

    private String normalizeTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return null;
        }
        return topic.trim();
    }
}
