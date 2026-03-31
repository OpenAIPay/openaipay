package cn.openaipay.domain.trade.service;

import org.joda.money.Money;

/**
 * 退款领域准备结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record TradeRefundPreparation(
        /** 金额 */
        Money amount,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId
) {
}
