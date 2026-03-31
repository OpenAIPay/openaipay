package cn.openaipay.domain.trade.client;

/**
 * TradePayRouteClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface TradePayRouteClient {
    TradeCreditRouteSnapshot routeCreditForTrade(String businessDomainCode,
                                                 String businessSceneCode,
                                                 String paymentMethod,
                                                 Long payerUserId,
                                                 Long payeeUserId);
}
