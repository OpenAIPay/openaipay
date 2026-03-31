package cn.openaipay.domain.accounting.repository;

import cn.openaipay.domain.accounting.model.AccountingEvent;
import cn.openaipay.domain.accounting.model.AccountingEventQuery;

import java.util.List;
import java.util.Optional;

/**
 * 会计事件仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingEventRepository {

    /**
     * 保存业务数据。
     */
    AccountingEvent save(AccountingEvent event);

    /**
     * 按事件ID查找记录。
     */
    Optional<AccountingEvent> findByEventId(String eventId);

    /**
     * 按KEY查找记录。
     */
    Optional<AccountingEvent> findByIdempotencyKey(String idempotencyKey);

    /**
     * 查询业务数据列表。
     */
    List<AccountingEvent> list(AccountingEventQuery query);
}
