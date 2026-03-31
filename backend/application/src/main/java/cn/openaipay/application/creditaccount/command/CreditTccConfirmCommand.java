package cn.openaipay.application.creditaccount.command;
/**
 * 信用TCCConfirm命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreditTccConfirmCommand(
        /** XID */
        String xid,
        /** 分支ID */
        String branchId
) {
}
