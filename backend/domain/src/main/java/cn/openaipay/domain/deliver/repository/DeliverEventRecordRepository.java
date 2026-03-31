package cn.openaipay.domain.deliver.repository;

import cn.openaipay.domain.deliver.model.DeliverEntityType;
import cn.openaipay.domain.deliver.model.DeliverEventRecord;
import cn.openaipay.domain.deliver.model.DeliverEventType;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 投放事件记录仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public interface DeliverEventRecordRepository {

    /** 统计指定时间窗口内的最近事件次数。 */
    long countRecent(String clientId,
                     Long userId,
                     DeliverEntityType entityType,
                     String entityCode,
                     DeliverEventType eventType,
                     LocalDateTime fromInclusive,
                     LocalDateTime toExclusive);

    /**
     * 保存ALL信息。
     */
    void saveAll(Collection<DeliverEventRecord> eventRecords);
}
