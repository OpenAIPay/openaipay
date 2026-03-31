package cn.openaipay.application.gateway.port;

/**
 * 银行入金确认请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record BankDepositConfirmRequest(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
