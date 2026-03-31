package cn.openaipay.domain.pay.client;

import org.joda.money.Money;

/**
 * PayInboundSubmitRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record PayInboundSubmitRequest(
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
        /** 付款方账号 */
        String payerAccountNo,
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
