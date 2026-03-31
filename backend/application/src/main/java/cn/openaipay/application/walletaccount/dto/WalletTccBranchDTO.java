package cn.openaipay.application.walletaccount.dto;
/**
 * 钱包TCC分支数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record WalletTccBranchDTO(
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
