package cn.openaipay.application.gateway.port;

/**
 * 银行提现撤销请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record BankWithdrawCancelRequest(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 业务原因 */
        String reason
) {
}
