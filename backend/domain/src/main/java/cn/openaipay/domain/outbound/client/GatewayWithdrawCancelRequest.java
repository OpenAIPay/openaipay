package cn.openaipay.domain.outbound.client;

/**
 * GatewayWithdrawCancelRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayWithdrawCancelRequest(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 业务原因 */
        String reason
) {
}
