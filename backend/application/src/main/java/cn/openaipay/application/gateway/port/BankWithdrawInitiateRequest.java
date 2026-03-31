package cn.openaipay.application.gateway.port;

import org.joda.money.Money;

/**
 * 银行提现受理请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record BankWithdrawInitiateRequest(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方账号 */
        String payeeAccountNo,
        /** 金额 */
        Money amount,
        /** 支付渠道编码 */
        String payChannelCode,
        /** 请求幂等标识 */
        String requestIdentify,
        /** 业务身份标识 */
        String bizIdentity
) {
}
