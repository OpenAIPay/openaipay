package cn.openaipay.application.outbound.command;

/**
 * 出金提现撤销命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record CancelOutboundWithdrawCommand(
        /** 出金单号 */
        String outboundId,
        /** 业务原因 */
        String reason
) {
}
