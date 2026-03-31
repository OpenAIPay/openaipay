package cn.openaipay.application.gateway.command;

/**
 * 网关入金查单命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayDepositQueryCommand(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
