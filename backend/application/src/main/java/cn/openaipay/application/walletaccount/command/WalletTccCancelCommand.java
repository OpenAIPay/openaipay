package cn.openaipay.application.walletaccount.command;

import org.joda.money.Money;
/**
 * 钱包TCCCancel命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record WalletTccCancelCommand(
        /** XID */
        String xid,
        /** 分支ID */
        String branchId,
        /** 用户ID */
        Long userId,
        /** 业务类型 */
        String operationType,
        /** 冻结类型 */
        String freezeType,
        /** 金额 */
        Money amount,
        /** 业务单号 */
        String businessNo
) {
}
