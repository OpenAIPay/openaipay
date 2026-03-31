package cn.openaipay.domain.inbound.client;

import org.joda.money.Money;

/**
 * GatewayDepositInitiateRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayDepositInitiateRequest(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 付款方账号 */
        String payerAccountNo,
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
