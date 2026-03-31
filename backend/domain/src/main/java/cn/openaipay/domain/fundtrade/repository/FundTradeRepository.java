package cn.openaipay.domain.fundtrade.repository;

import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 基金交易仓储接口。
 *
 * 业务场景：
 * - 聚焦基金交易状态机（冻结/确认/补偿、申购/赎回/切换/收益结转）；
 * - 与基金账户头寸仓储解耦，避免 fundaccount 聚合承担交易状态机职责。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public interface FundTradeRepository {

    /**
     * 按订单号查询基金交易。
     */
    Optional<FundTransaction> findTransaction(String orderNo);

    /**
     * 按订单号加锁查询基金交易。
     */
    Optional<FundTransaction> findTransactionForUpdate(String orderNo);

    /**
     * 按业务编号查询基金交易。
     */
    Optional<FundTransaction> findTransactionByBusinessNoAndType(Long userId,
                                                                 String fundCode,
                                                                 FundTransactionType transactionType,
                                                                 String businessNo);

    /**
     * 按基金编码与交易类型批量查询最近基金交易。
     */
    List<FundTransaction> findRecentTransactionsByTypes(String fundCode,
                                                        List<FundTransactionType> transactionTypes,
                                                        int limit);

    /**
     * 按用户、基金、类型查询指定更新时间范围内已确认交易。
     *
     * 业务场景：收益发放按 T-1 份额计算时，需要把“当日确认的份额变更”从当前持仓回推剔除。
     */
    List<FundTransaction> findConfirmedTransactionsUpdatedInRange(Long userId,
                                                                  String fundCode,
                                                                  LocalDateTime updatedFromInclusive,
                                                                  LocalDateTime updatedToInclusive,
                                                                  List<FundTransactionType> transactionTypes);

    /**
     * 保存基金交易。
     */
    FundTransaction saveTransaction(FundTransaction transaction);
}
