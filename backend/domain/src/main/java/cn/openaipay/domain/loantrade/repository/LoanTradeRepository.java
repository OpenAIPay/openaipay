package cn.openaipay.domain.loantrade.repository;

import cn.openaipay.domain.loantrade.model.LoanTradeOrder;

import java.util.Optional;

/**
 * 爱借交易仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public interface LoanTradeRepository {

    /**
     * 按XID与ID查找记录。
     */
    Optional<LoanTradeOrder> findByXidAndBranchId(String xid, String branchId);

    /**
     * 按XID与ID查找记录并加锁。
     */
    Optional<LoanTradeOrder> findByXidAndBranchIdForUpdate(String xid, String branchId);

    /**
     * 保存业务数据。
     */
    LoanTradeOrder save(LoanTradeOrder loanTradeOrder);
}
