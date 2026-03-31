package cn.openaipay.application.fundaccount.command;
/**
 * 创建基金账户命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateFundAccountCommand(
        /** 用户ID */
        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode
) {
}
