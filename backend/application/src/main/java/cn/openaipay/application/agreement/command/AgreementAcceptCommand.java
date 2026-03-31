package cn.openaipay.application.agreement.command;

/**
 * 协议同意项命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record AgreementAcceptCommand(
        /** 模板编码 */
        String templateCode,
        /** 模板版本号 */
        String templateVersion
) {
}
