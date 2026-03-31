package cn.openaipay.application.loanaccount.dto;

/**
 * 爱借账户账本分支结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/28
 */
public record LoanAccountLedgerBranchDTO(
        /** 全局事务XID。 */
        String xid,
        /** 分支编号。 */
        String branchId,
        /** 分支状态。 */
        String branchStatus,
        /** 结果说明。 */
        String message
) {
}
