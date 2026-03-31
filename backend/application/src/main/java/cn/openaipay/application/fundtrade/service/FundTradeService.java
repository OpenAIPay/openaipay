package cn.openaipay.application.fundtrade.service;

import cn.openaipay.application.fundaccount.command.FundSubscribeCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeConfirmCommand;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;

/**
 * 爱存交易应用服务接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
public interface FundTradeService {

    /**
     * 同步提交到统一交易主链路。
     */
    TradeOrderDTO submitToTrade(CreatePayTradeCommand command);

    /**
     * 爱存交易 TRY。
     */
    FundTransactionDTO trySubscribe(FundSubscribeCommand command);

    /**
     * 爱存交易 CONFIRM。
     */
    FundTransactionDTO confirmSubscribe(FundSubscribeConfirmCommand command);

    /**
     * 爱存交易 CANCEL。
     */
    FundTransactionDTO cancelSubscribe(FundSubscribeCancelCommand command);
}
