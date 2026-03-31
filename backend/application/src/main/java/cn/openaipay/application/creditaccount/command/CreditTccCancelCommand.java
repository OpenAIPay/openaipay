package cn.openaipay.application.creditaccount.command;

import org.joda.money.Money;
/**
 * 信用TCCCancel命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreditTccCancelCommand(
        /** XID */
        String xid,
        /** 分支ID */
        String branchId,
        /** 业务单号 */
        String accountNo,
        /** 业务类型 */
        String operationType,
        /** 资源信息 */
        String assetCategory,
        /** 金额 */
        Money amount,
        /** 业务单号 */
        String businessNo
) {
}
