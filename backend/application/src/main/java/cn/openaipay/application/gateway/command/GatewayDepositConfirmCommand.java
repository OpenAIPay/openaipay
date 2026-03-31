package cn.openaipay.application.gateway.command;

/**
 * 网关入金确认命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayDepositConfirmCommand(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
