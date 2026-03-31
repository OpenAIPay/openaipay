package cn.openaipay.application.walletaccount.command;
/**
 * 钱包TCCConfirm命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record WalletTccConfirmCommand(
        /** XID */
        String xid,
        /** 分支ID */
        String branchId
) {
}
