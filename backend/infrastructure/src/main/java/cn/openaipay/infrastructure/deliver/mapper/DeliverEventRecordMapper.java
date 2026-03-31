package cn.openaipay.infrastructure.deliver.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverEventRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;

/**
 * DeliverEventRecordMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Mapper
public interface DeliverEventRecordMapper extends BaseMapper<DeliverEventRecordDO> {

    /**
     * 处理数量信息。
     */
    default long countRecent(String clientId,
                             Long userId,
                             String entityType,
                             String entityCode,
                             String eventType,
                             LocalDateTime fromInclusive,
                             LocalDateTime toExclusive) {
        QueryWrapper<DeliverEventRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("client_id", clientId)
                .eq("entity_type", entityType)
                .eq("entity_code", entityCode)
                .eq("event_type", eventType)
                .ge("event_time", fromInclusive)
                .lt("event_time", toExclusive);
        if (userId != null) {
            wrapper.and(query -> query.eq("user_id", userId).or().isNull("user_id"));
        }
        Long count = selectCount(wrapper);
        return count == null ? 0L : count;
    }
}
