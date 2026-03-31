package cn.openaipay.infrastructure.accounting.mapper;

import cn.openaipay.infrastructure.accounting.dataobject.AccountingEntryDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会计分录Mapper。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Mapper
public interface AccountingEntryMapper extends BaseMapper<AccountingEntryDO> {
}
