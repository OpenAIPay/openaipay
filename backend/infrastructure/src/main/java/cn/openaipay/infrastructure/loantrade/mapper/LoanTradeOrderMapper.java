package cn.openaipay.infrastructure.loantrade.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.loantrade.dataobject.LoanTradeOrderDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 爱借交易单持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Mapper
public interface LoanTradeOrderMapper extends BaseMapper<LoanTradeOrderDO> {

    /**
     * 按XID与ID查找记录。
     */
    default Optional<LoanTradeOrderDO> findByXidAndBranchId(String xid, String branchId) {
        QueryWrapper<LoanTradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按XID与ID查找记录并加锁。
     */
    default Optional<LoanTradeOrderDO> findByXidAndBranchIdForUpdate(String xid, String branchId) {
        QueryWrapper<LoanTradeOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
