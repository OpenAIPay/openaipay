package cn.openaipay.application.trade.command;

import org.joda.money.Money;

/**
 * 创建支付交易命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CreatePayTradeCommand(
        /** 请求幂等号 */
        String requestNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 支付方式编码 */
        String paymentMethod,
        /** 金额 */
        Money amount,
        /** 钱包金额 */
        Money walletDebitAmount,
        /** 资金金额 */
        Money fundDebitAmount,
        /** 信用金额 */
        Money creditDebitAmount,
        /** 入金金额 */
        Money inboundDebitAmount,
        /** 优惠券单号 */
        String couponNo,
        /** 业务编码 */
        String paymentToolCode,
        /** 扩展信息 */
        String metadata
) {
}
