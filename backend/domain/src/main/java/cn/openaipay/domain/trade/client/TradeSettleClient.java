package cn.openaipay.domain.trade.client;

/**
 * TradeSettleClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface TradeSettleClient {
    /**
     * 处理结算交易信息。
     */
    TradeSettleResult settleCommittedTrade(TradeSettleRequest request);
}
