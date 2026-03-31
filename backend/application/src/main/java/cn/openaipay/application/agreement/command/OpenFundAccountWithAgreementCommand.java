package cn.openaipay.application.agreement.command;

import java.util.List;

/**
 * 签约并开通爱存基金账户命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record OpenFundAccountWithAgreementCommand(
        /** 用户ID */
        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode,
        /** 业务键 */
        String idempotencyKey,
        /** 协议信息 */
        List<AgreementAcceptCommand> agreementAccepts
) {
}
