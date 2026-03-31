package cn.openaipay.application.gateway.command;

/**
 * 网关提现确认命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayWithdrawConfirmCommand(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
