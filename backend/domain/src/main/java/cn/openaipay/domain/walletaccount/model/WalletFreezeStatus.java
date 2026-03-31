package cn.openaipay.domain.walletaccount.model;

/**
 * 钱包冻结状态
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum WalletFreezeStatus {
    /** 已冻结 */
    FROZEN,
    /** 已解冻 */
    RELEASED,
    /** 已扣减 */
    DEDUCTED
}
