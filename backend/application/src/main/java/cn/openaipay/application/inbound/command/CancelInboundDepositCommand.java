package cn.openaipay.application.inbound.command;

/**
 * 入金撤销命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record CancelInboundDepositCommand(
        /** 入金单号 */
        String inboundId,
        /** 业务原因 */
        String reason
) {
}
