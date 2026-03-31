package cn.openaipay.application.gateway.command;

/**
 * 网关提现查单命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayWithdrawQueryCommand(
        /** 出金单号 */
        String outboundId,
        /** 机构渠道编码 */
        String instChannelCode
) {
}
