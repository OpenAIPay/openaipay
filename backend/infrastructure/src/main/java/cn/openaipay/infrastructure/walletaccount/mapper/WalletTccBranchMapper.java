package cn.openaipay.infrastructure.walletaccount.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.walletaccount.dataobject.WalletTccBranchDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包TCC分支持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface WalletTccBranchMapper extends BaseMapper<WalletTccBranchDO> {

    /**
     * 按XID与ID查找记录。
     */
    default Optional<WalletTccBranchDO> findByXidAndBranchId(String xid, String branchId) {
        QueryWrapper<WalletTccBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按XID与ID查找记录并加锁。
     */
    default Optional<WalletTccBranchDO> findByXidAndBranchIdForUpdate(String xid, String branchId) {
        QueryWrapper<WalletTccBranchDO> wrapper = new QueryWrapper<>();
        wrapper.eq("xid", xid);
        wrapper.eq("branch_id", branchId);
        wrapper.last("LIMIT 1 FOR UPDATE");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
