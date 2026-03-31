package cn.openaipay.application.loanaccount.dto;

/**
 * 借贷账户 TCC 分支数据传输对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record LoanTccBranchDTO(
        /** XID */
        String xid,
        /** 分支ID */
        String branchId,
        /** 分支状态 */
        String branchStatus,
        /** 消息内容 */
        String message
) {
}
