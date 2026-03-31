package cn.openaipay.application.settle.command;

import org.joda.money.Money;

/**
 * 已支付成功交易的结算命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SettleCommittedTradeCommand(
        /** 交易类型 */
        String tradeType,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 支付单号 */
        String payOrderNo,
        /** 请求幂等号 */
        String requestNo,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 计费单号 */
        String pricingQuoteNo,
        /** 结算金额 */
        Money settleAmount,
        /** 原始金额 */
        Money originalAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 信用收款方信息 */
        boolean shouldCreditPayee
) {
}
