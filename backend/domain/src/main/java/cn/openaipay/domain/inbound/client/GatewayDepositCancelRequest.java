package cn.openaipay.domain.inbound.client;

/**
 * GatewayDepositCancelRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayDepositCancelRequest(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 业务原因 */
        String reason
) {
}
