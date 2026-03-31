package cn.openaipay.application.walletaccount.command;

/**
 * 钱包解冻命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeReleaseCommand(
        /** 用户ID */
        Long userId,
        /** 冻结号 */
        String freezeNo,
        /** 解冻原因 */
        String reason
) {
}
