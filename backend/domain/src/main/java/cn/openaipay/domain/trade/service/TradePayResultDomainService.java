package cn.openaipay.domain.trade.service;

/**
 * 交易域支付结果决策服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface TradePayResultDomainService {

    /**
     * 将支付域状态折叠为交易域处理决策。
     */
    TradePayResultDecision decide(String payStatus, String resultCode, String resultMessage);
}
