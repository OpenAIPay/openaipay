package cn.openaipay.domain.trade.service;

import cn.openaipay.domain.trade.model.TradeOrder;
import org.joda.money.Money;

/**
 * 交易退款领域服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface TradeRefundDomainService {

    /**
     * 校验并准备退款领域参数。
     */
    TradeRefundPreparation prepareRefund(
            TradeOrder originalTrade,
            Money refundAmount,
            Money refundedAmount,
            Long requestedPayerUserId,
            Long requestedPayeeUserId
    );
}
