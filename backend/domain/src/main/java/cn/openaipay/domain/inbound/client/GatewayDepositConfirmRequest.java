package cn.openaipay.domain.inbound.client;

/**
 * GatewayDepositConfirmRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayDepositConfirmRequest(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
