package cn.openaipay.domain.trade.client;

import org.joda.money.Money;

/**
 * TradePaySubmitRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record TradePaySubmitRequest(
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 来源交易类型 */
        String sourceTradeType,
        /** 结算金额 */
        Money settleAmount,
        /** 收款方信用信息 */
        Boolean requiresPayeeCredit,
        /** 订单单号 */
        String bizOrderNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 原始金额 */
        Money originalAmount,
        /** 钱包金额 */
        Money walletDebitAmount,
        /** 资金金额 */
        Money fundDebitAmount,
        /** 信用金额 */
        Money creditDebitAmount,
        /** 入金金额 */
        Money inboundDebitAmount,
        /** 出金金额 */
        Money outboundAmount,
        /** 优惠券单号 */
        String couponNo,
        /** 资金编码 */
        String fundCode,
        /** 业务编码 */
        String paymentToolCode,
        /** 支付方式编码 */
        String paymentMethod
) {
}
