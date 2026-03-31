package cn.openaipay.application.gateway.port;

/**
 * 银行提现查单请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record BankWithdrawQueryRequest(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
