package cn.openaipay.infrastructure.walletaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletFreezeRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 钱包冻结明细持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
@Mapper
public interface WalletFreezeRecordMapper extends BaseMapper<WalletFreezeRecordDO> {

    /**
     * 按XID与ID查找记录并加锁。
     */
    default Optional<WalletFreezeRecordDO> findByXidAndBranchIdForUpdate(String xid, String branchId) {
        QueryWrapper<WalletFreezeRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按单号查找记录并加锁。
     */
    default Optional<WalletFreezeRecordDO> findManualByFreezeNoForUpdate(Long userId, String freezeNo) {
        QueryWrapper<WalletFreezeRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("xid", freezeNo);
        wrapper.eq("branch_id", freezeNo);
        wrapper.eq("business_no", freezeNo);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户与查询记录列表。
     */
    default List<WalletFreezeRecordDO> listByUserAndFilters(Long userId,
                                                             String currencyCode,
                                                             String freezeType,
                                                             String freezeStatus,
                                                             int limit) {
        QueryWrapper<WalletFreezeRecordDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (currencyCode != null && !currencyCode.isBlank()) {
            wrapper.eq("currency_code", currencyCode);
        }
        if (freezeType != null && !freezeType.isBlank()) {
            wrapper.eq("freeze_type", freezeType);
        }
        if (freezeStatus != null && !freezeStatus.isBlank()) {
            wrapper.eq("freeze_status", freezeStatus);
        }
        wrapper.orderByDesc("created_at");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }
}
