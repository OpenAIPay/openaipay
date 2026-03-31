package cn.openaipay.domain.fundaccount.repository;

import cn.openaipay.domain.fundaccount.model.FundAccount;
import cn.openaipay.domain.fundaccount.model.FundFastRedeemQuota;
import cn.openaipay.domain.fundaccount.model.FundIncomeCalendar;
import cn.openaipay.domain.fundaccount.model.FundProduct;
import cn.openaipay.domain.fundaccount.model.FundTransaction;
import cn.openaipay.domain.fundaccount.model.FundTransactionType;
import cn.openaipay.domain.fundaccount.model.FundUserFastRedeemQuota;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 基金账户仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface FundAccountRepository {

    /**
     * 按用户ID和基金编码查询基金账户。
     */
    Optional<FundAccount> findByUserIdAndFundCode(Long userId, String fundCode);

    /**
     * 按用户ID和基金编码加锁查询基金账户。
     */
    Optional<FundAccount> findByUserIdAndFundCodeForUpdate(Long userId, String fundCode);

    /**
     * 按用户ID查询全部基金账户。
     */
    List<FundAccount> findAllByUserId(Long userId);

    /**
     * 按基金编码查询全部基金账户。
     */
    List<FundAccount> findAllByFundCode(String fundCode);

    /**
     * 保存基金账户。
     */
    FundAccount save(FundAccount fundAccount);

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
     *
     * 业务场景：爱存历史回填任务按类型扫描旧交易，补齐 trade_fund_order 扩展单。
     */
    List<FundTransaction> findRecentTransactionsByTypes(String fundCode,
                                                        List<FundTransactionType> transactionTypes,
                                                        int limit);

    /**
     * 保存基金交易。
     */
    FundTransaction saveTransaction(FundTransaction transaction);

    /**
     * 按基金编码查询产品。
     */
    Optional<FundProduct> findProduct(String fundCode);

    /**
     * 按基金编码加锁查询产品。
     */
    Optional<FundProduct> findProductForUpdate(String fundCode);

    /**
     * 保存基金产品。
     */
    FundProduct saveProduct(FundProduct product);

    /**
     * 按基金编码和业务日期查询收益日历。
     */
    Optional<FundIncomeCalendar> findIncomeCalendar(String fundCode, LocalDate bizDate);

    /**
     * 按基金编码和业务日期加锁查询收益日历。
     */
    Optional<FundIncomeCalendar> findIncomeCalendarForUpdate(String fundCode, LocalDate bizDate);

    /**
     * 保存收益日历。
     */
    FundIncomeCalendar saveIncomeCalendar(FundIncomeCalendar calendar);

    /**
     * 按基金编码和日期加锁查询快速赎回额度。
     */
    Optional<FundFastRedeemQuota> findFastRedeemQuotaForUpdate(String fundCode, LocalDate quotaDate);

    /**
     * 保存快速赎回额度。
     */
    FundFastRedeemQuota saveFastRedeemQuota(FundFastRedeemQuota quota);

    /**
     * 按基金编码、用户ID和日期加锁查询用户快速赎回额度。
     */
    Optional<FundUserFastRedeemQuota> findUserFastRedeemQuotaForUpdate(String fundCode, Long userId, LocalDate quotaDate);

    /**
     * 保存用户快速赎回额度。
     */
    FundUserFastRedeemQuota saveUserFastRedeemQuota(FundUserFastRedeemQuota quota);
}
