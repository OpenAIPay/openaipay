package cn.openaipay.application.gateway.port;

/**
 * 银行入金查单请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record BankDepositQueryRequest(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
