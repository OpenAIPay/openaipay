package cn.openaipay.application.outbound.command;

import org.joda.money.Money;

/**
 * 出金提现提交命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SubmitOutboundWithdrawCommand(
        /** 请求业务单号 */
        String requestBizNo,
        /** 业务单号 */
        String bizOrderNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方账号 */
        String payeeAccountNo,
        /** 金额 */
        Money amount,
        /** 支付渠道编码 */
        String payChannelCode,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 请求幂等标识 */
        String requestIdentify,
        /** 业务身份标识 */
        String bizIdentity
) {
}
