package cn.openaipay.infrastructure.accounting.mapper;

import cn.openaipay.infrastructure.accounting.dataobject.AccountingEventJournalDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 会计事件日志Mapper。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Mapper
public interface AccountingEventJournalMapper extends BaseMapper<AccountingEventJournalDO> {

    /**
     * 按事件ID查找记录。
     */
    default Optional<AccountingEventJournalDO> findByEventId(String eventId) {
        QueryWrapper<AccountingEventJournalDO> wrapper = new QueryWrapper<>();
        wrapper.eq("event_id", eventId).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按KEY查找记录。
     */
    default Optional<AccountingEventJournalDO> findByIdempotencyKey(String idempotencyKey) {
        QueryWrapper<AccountingEventJournalDO> wrapper = new QueryWrapper<>();
        wrapper.eq("idempotency_key", idempotencyKey).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
