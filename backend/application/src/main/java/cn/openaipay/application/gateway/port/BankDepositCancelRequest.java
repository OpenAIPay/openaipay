package cn.openaipay.application.gateway.port;

/**
 * 银行入金撤销请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record BankDepositCancelRequest(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 业务原因 */
        String reason
) {
}
