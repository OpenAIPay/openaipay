package cn.openaipay.application.gateway.port;

/**
 * 银行提现确认请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record BankWithdrawConfirmRequest(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
