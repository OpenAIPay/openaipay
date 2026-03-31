package cn.openaipay.domain.accounting.repository;

import cn.openaipay.domain.accounting.model.AccountingEntry;
import cn.openaipay.domain.accounting.model.AccountingEntryQuery;

import java.util.List;

/**
 * 会计分录仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface AccountingEntryRepository {

    /**
     * 保存ALL信息。
     */
    List<AccountingEntry> saveAll(List<AccountingEntry> entries);

    /**
     * 查询业务数据列表。
     */
    List<AccountingEntry> list(AccountingEntryQuery query);
}
