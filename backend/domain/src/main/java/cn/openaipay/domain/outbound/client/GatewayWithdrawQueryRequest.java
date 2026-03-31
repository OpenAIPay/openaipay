package cn.openaipay.domain.outbound.client;

/**
 * GatewayWithdrawQueryRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayWithdrawQueryRequest(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
