package cn.openaipay.infrastructure.trade;

import cn.openaipay.application.settle.command.SettleCommittedTradeCommand;
import cn.openaipay.application.settle.dto.SettleResultDTO;
import cn.openaipay.application.settle.facade.SettleFacade;
import cn.openaipay.domain.trade.client.TradeSettleClient;
import cn.openaipay.domain.trade.client.TradeSettleRequest;
import cn.openaipay.domain.trade.client.TradeSettleResult;
import org.springframework.stereotype.Component;

/**
 * TradeSettleClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class TradeSettleClientImpl implements TradeSettleClient {

    /** 结算信息 */
    private final SettleFacade settleFacade;

    public TradeSettleClientImpl(SettleFacade settleFacade) {
        this.settleFacade = settleFacade;
    }

    /**
     * 处理结算交易信息。
     */
    @Override
    public TradeSettleResult settleCommittedTrade(TradeSettleRequest request) {
        SettleResultDTO result = settleFacade.settleCommittedTrade(new SettleCommittedTradeCommand(
                request.tradeType(),
                request.payerUserId(),
                request.payeeUserId(),
                request.payOrderNo(),
                request.requestNo(),
                request.tradeOrderNo(),
                request.pricingQuoteNo(),
                request.settleAmount(),
                request.originalAmount(),
                request.payableAmount(),
                request.shouldCreditPayee()
        ));
        return new TradeSettleResult(result.status(), result.message());
    }
}
