package cn.openaipay.application.outbox.facade;

import cn.openaipay.application.outbox.dto.OutboxMessageDTO;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.dto.OutboxTopicDistributionDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息投递中心门面（Outbox）
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface OutboxMonitorFacade {

    /**
     * 查询概览。
     */
    OutboxOverviewDTO getOverview();

    /**
     * 查询主题分布。
     */
    List<OutboxTopicDistributionDTO> listTopicDistribution(Integer limit);

    /**
     * 查询消息列表。
     */
    List<OutboxMessageDTO> listMessages(String topic,
                                        String status,
                                        String keyword,
                                        Boolean onlyRetried,
                                        Integer limit,
                                        Boolean includePayload);

    /**
     * 查询死信列表。
     */
    List<OutboxMessageDTO> listDeadLetters(String topic,
                                           String keyword,
                                           Integer limit,
                                           Boolean includePayload);

    /**
     * 查询单条消息。
     */
    OutboxMessageDTO getMessage(Long id);

    /**
     * 手工重放单条死信。
     */
    boolean requeueDeadLetter(Long id, LocalDateTime nextRetryAt, String operator);

    /**
     * 手工批量重放死信。
     */
    int requeueDeadLetters(String topic, Integer limit, LocalDateTime nextRetryAt, String operator);
}
