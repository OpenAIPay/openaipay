package cn.openaipay.infrastructure.accounting.mapper;

import cn.openaipay.infrastructure.accounting.dataobject.AccountingVoucherDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 会计凭证Mapper。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Mapper
public interface AccountingVoucherMapper extends BaseMapper<AccountingVoucherDO> {

    /**
     * 按凭证单号查找记录。
     */
    default Optional<AccountingVoucherDO> findByVoucherNo(String voucherNo) {
        QueryWrapper<AccountingVoucherDO> wrapper = new QueryWrapper<>();
        wrapper.eq("voucher_no", voucherNo).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按事件ID查找记录。
     */
    default Optional<AccountingVoucherDO> findByEventId(String eventId) {
        QueryWrapper<AccountingVoucherDO> wrapper = new QueryWrapper<>();
        wrapper.eq("event_id", eventId).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
