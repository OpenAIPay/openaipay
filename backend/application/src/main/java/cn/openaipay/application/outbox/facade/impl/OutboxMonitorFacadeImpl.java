package cn.openaipay.application.outbox.facade.impl;

import cn.openaipay.application.outbox.dto.OutboxMessageDTO;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.dto.OutboxTopicDistributionDTO;
import cn.openaipay.application.outbox.facade.OutboxMonitorFacade;
import cn.openaipay.application.outbox.service.OutboxMonitorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息投递中心门面实现（Outbox）
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
@Service
public class OutboxMonitorFacadeImpl implements OutboxMonitorFacade {

    private final OutboxMonitorService outboxMonitorService;

    public OutboxMonitorFacadeImpl(OutboxMonitorService outboxMonitorService) {
        this.outboxMonitorService = outboxMonitorService;
    }

    @Override
    public OutboxOverviewDTO getOverview() {
        return outboxMonitorService.getOverview();
    }

    @Override
    public List<OutboxTopicDistributionDTO> listTopicDistribution(Integer limit) {
        return outboxMonitorService.listTopicDistribution(limit);
    }

    @Override
    public List<OutboxMessageDTO> listMessages(String topic,
                                               String status,
                                               String keyword,
                                               Boolean onlyRetried,
                                               Integer limit,
                                               Boolean includePayload) {
        return outboxMonitorService.listMessages(topic, status, keyword, onlyRetried, limit, includePayload);
    }

    @Override
    public List<OutboxMessageDTO> listDeadLetters(String topic,
                                                  String keyword,
                                                  Integer limit,
                                                  Boolean includePayload) {
        return outboxMonitorService.listDeadLetters(topic, keyword, limit, includePayload);
    }

    @Override
    public OutboxMessageDTO getMessage(Long id) {
        return outboxMonitorService.getMessage(id);
    }

    @Override
    public boolean requeueDeadLetter(Long id, LocalDateTime nextRetryAt, String operator) {
        return outboxMonitorService.requeueDeadLetter(id, nextRetryAt, operator);
    }

    @Override
    public int requeueDeadLetters(String topic, Integer limit, LocalDateTime nextRetryAt, String operator) {
        return outboxMonitorService.requeueDeadLetters(topic, limit, nextRetryAt, operator);
    }
}
