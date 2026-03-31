package cn.openaipay.domain.trade.service;

import cn.openaipay.domain.trade.model.TradeSplitPlan;
import cn.openaipay.domain.trade.model.TradeType;
import org.joda.money.Money;

/**
 * 交易参与方拆分领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface TradeSplitDomainService {

    /**
     * 解析交易在支付域中的资金拆分方案。
     */
    TradeSplitPlan resolveSplitPlan(
            TradeType tradeType,
            String paymentMethod,
            Money payableAmount,
            Money walletDebitAmount,
            Money fundDebitAmount,
            Money creditDebitAmount,
            Money inboundDebitAmount
    );
}
