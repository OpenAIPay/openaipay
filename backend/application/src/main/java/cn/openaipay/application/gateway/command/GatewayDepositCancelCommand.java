package cn.openaipay.application.gateway.command;

/**
 * 网关入金撤销命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayDepositCancelCommand(
        /** 入金单号 */
        String inboundId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 业务原因 */
        String reason
) {
}
