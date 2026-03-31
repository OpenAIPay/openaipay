package cn.openaipay.application.fundtrade.service.impl;

import cn.openaipay.application.fundaccount.command.FundSubscribeCancelCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeCommand;
import cn.openaipay.application.fundaccount.command.FundSubscribeConfirmCommand;
import cn.openaipay.application.fundaccount.dto.FundTransactionDTO;
import cn.openaipay.application.fundaccount.service.FundAccountService;
import cn.openaipay.application.fundtrade.service.FundTradeService;
import cn.openaipay.application.trade.command.CreatePayTradeCommand;
import cn.openaipay.application.trade.dto.TradeOrderDTO;
import cn.openaipay.application.trade.facade.TradeFacade;
import org.springframework.stereotype.Service;

/**
 * 爱存交易应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/27
 */
@Service
public class FundTradeServiceImpl implements FundTradeService {

    /** 统一交易门面。 */
    private final TradeFacade tradeFacade;
    /** 爱存账户应用服务。 */
    private final FundAccountService fundAccountService;

    public FundTradeServiceImpl(TradeFacade tradeFacade,
                                FundAccountService fundAccountService) {
        this.tradeFacade = tradeFacade;
        this.fundAccountService = fundAccountService;
    }

    /**
     * 同步提交到统一交易主链路。
     */
    @Override
    public TradeOrderDTO submitToTrade(CreatePayTradeCommand command) {
        return tradeFacade.pay(command);
    }

    /**
     * 爱存交易 TRY。
     */
    @Override
    public FundTransactionDTO trySubscribe(FundSubscribeCommand command) {
        return fundAccountService.subscribe(command);
    }

    /**
     * 爱存交易 CONFIRM。
     */
    @Override
    public FundTransactionDTO confirmSubscribe(FundSubscribeConfirmCommand command) {
        return fundAccountService.confirmSubscribe(command);
    }

    /**
     * 爱存交易 CANCEL。
     */
    @Override
    public FundTransactionDTO cancelSubscribe(FundSubscribeCancelCommand command) {
        return fundAccountService.cancelSubscribe(command);
    }
}
