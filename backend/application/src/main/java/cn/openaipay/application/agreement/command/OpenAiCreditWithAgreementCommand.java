package cn.openaipay.application.agreement.command;

import java.util.List;

/**
 * 签约并开通爱花命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record OpenAiCreditWithAgreementCommand(
        /** 用户ID */
        Long userId,
        /** 业务键 */
        String idempotencyKey,
        /** 协议信息 */
        List<AgreementAcceptCommand> agreementAccepts
) {
}
