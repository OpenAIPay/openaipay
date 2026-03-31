package cn.openaipay.application.walletaccount.command;
/**
 * 创建钱包账户命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateWalletAccountCommand(
        /** 用户ID */
        Long userId,
        /** 币种编码 */
        String currencyCode
) {
}
