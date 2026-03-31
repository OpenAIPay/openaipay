package cn.openaipay.application.loanaccount.command;

/**
 * 借贷账户 TCC Confirm 命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record LoanTccConfirmCommand(
        /** XID */
        String xid,
        /** 分支ID */
        String branchId
) {
}
