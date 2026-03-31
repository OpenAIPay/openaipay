package cn.openaipay.application.walletaccount.command;

/**
 * 钱包冻结扣减命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeDeductCommand(
        /** 用户ID */
        Long userId,
        /** 冻结号 */
        String freezeNo,
        /** 扣减原因 */
        String reason
) {
}
