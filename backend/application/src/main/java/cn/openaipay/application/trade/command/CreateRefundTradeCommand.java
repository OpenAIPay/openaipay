package cn.openaipay.application.trade.command;

import org.joda.money.Money;

/**
 * 创建Refund交易命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CreateRefundTradeCommand(
        /** 请求幂等号 */
        String requestNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 原始交易订单单号 */
        String originalTradeOrderNo,
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
        /** 扩展信息 */
        String metadata
) {
}
