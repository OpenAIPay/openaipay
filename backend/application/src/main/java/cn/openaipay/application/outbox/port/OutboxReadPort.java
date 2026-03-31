package cn.openaipay.application.outbox.port;

import cn.openaipay.application.outbox.dto.OutboxMessageDTO;
import cn.openaipay.application.outbox.dto.OutboxOverviewDTO;
import cn.openaipay.application.outbox.dto.OutboxTopicDistributionDTO;

import java.util.List;

/**
 * Outbox 只读查询端口
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface OutboxReadPort {

    /**
     * 查询概览。
     */
    OutboxOverviewDTO getOverview(long processingTimeoutSeconds);

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
     * 查询单条消息。
     */
    OutboxMessageDTO getMessage(Long id);
}
