package cn.openaipay.application.loanaccount.command;

import org.joda.money.Money;

/**
 * 借贷账户 TCC Try 命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record LoanTccTryCommand(
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
