package cn.openaipay.application.fundaccount.facade;

import cn.openaipay.application.fundaccount.command.CreateFundAccountCommand;
import cn.openaipay.application.fundaccount.command.FundFastRedeemCommand;
import cn.openaipay.application.fundaccount.command.FundIncomeSettleCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemCancelCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemCommand;
import cn.openaipay.application.fundaccount.command.FundRedeemConfirmCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeConfirmCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchCommand;
import cn.openaipay.application.fundaccount.command.FundSwitchConfirmCommand;
import cn.openaipay.application.fundaccount.command.PublishFundIncomeCalendarCommand;
import cn.openaipay.application.fundaccount.command.SettleFundIncomeCalendarCommand;
import cn.openaipay.application.fundaccount.command.UpsertFundProductCommand;
import cn.openaipay.application.fundaccount.dto.FundAccountDTO;
import cn.openaipay.application.fundaccount.dto.FundIncomeCalendarDTO;
import cn.openaipay.application.fundaccount.dto.FundProductDTO;
import cn.openaipay.application.fundaccount.dto.FundTradeBackfillResultDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDetailDTO;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import org.joda.money.Money;
import java.util.List;

/**
 * 基金账户门面接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface FundAccountFacade {

    /**
     * 创建基金信息。
     */
    Long createFundAccount(CreateFundAccountCommand command);

    /**
     * 获取基金信息。
     */
    FundAccountDTO getFundAccount(Long userId);

    /**
     * 获取基金信息。
     */
    FundAccountDTO getFundAccount(Long userId, String fundCode);

    /**
     * 保存或更新基金信息。
     */
    FundProductDTO upsertFundProduct(UpsertFundProductCommand command);

    /**
     * 获取基金信息。
     */
    FundProductDTO getFundProduct(String fundCode);

    /**
     * 处理业务数据。
     */
    FundTransactionDTO subscribe(FundSubscribeCommand command);

    /**
     * 确认业务数据。
     */
    FundTransactionDTO confirmSubscribe(FundSubscribeConfirmCommand command);

    /**
     * 取消业务数据。
     */
    FundTransactionDTO cancelSubscribe(FundSubscribeCancelCommand command);

    /**
     * 处理业务数据。
     */
    FundTransactionDTO redeem(FundRedeemCommand command);

    /**
     * 确认业务数据。
     */
    FundTransactionDTO confirmRedeem(FundRedeemConfirmCommand command);

    /**
     * 取消业务数据。
     */
    FundTransactionDTO cancelRedeem(FundRedeemCancelCommand command);

    /**
     * 处理业务数据。
     */
    FundTransactionDTO fastRedeem(FundFastRedeemCommand command);

    /**
     * 处理业务数据。
     */
    FundTransactionDTO switchProduct(FundSwitchCommand command);

    /**
     * 确认业务数据。
     */
    FundTransactionDTO confirmSwitch(FundSwitchConfirmCommand command);

    /**
     * 取消业务数据。
     */
    FundTransactionDTO cancelSwitch(FundSwitchCancelCommand command);

    /**
     * 处理结算收益信息。
     */
    FundTransactionDTO settleIncome(FundIncomeSettleCommand command);

    /**
     * 发布收益日历信息。
     */
    FundIncomeCalendarDTO publishIncomeCalendar(PublishFundIncomeCalendarCommand command);

    /**
     * 处理结算收益日历信息。
     */
    FundIncomeCalendarDTO settleIncomeCalendar(SettleFundIncomeCalendarCommand command);

    /**
     * 解析基金编码。
     */
    String resolveFundCode(Long userId, String preferredFundCode);

    /** 冻结支付占用份额。 */
    FundFreezeResult freezeShareForPay(String fundTradeNo,
                                       Long userId,
                                       String preferredFundCode,
                                       Money amount,
                                       String businessNo);

    /**
     * 确认份额用于支付信息。
     */
    void confirmFrozenShareForPay(Long userId, String fundTradeNo);

    /** 补偿支付冻结份额。 */
    void compensateFrozenShareForPay(Long userId,
                                     String fundTradeNo,
                                     String preferredFundCode,
                                     String businessNo);

    /**
     * 获取基金明细信息。
     */
    FundTransactionDetailDTO getFundTransactionDetail(String fundTradeNo);

    /** 回填基金交易扩展单。 */
    FundTradeBackfillResultDTO backfillTradeFundOrders(String fundCode,
                                                       List<String> transactionTypes,
                                                       Integer limit);
}
