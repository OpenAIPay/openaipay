package cn.openaipay.domain.walletaccount.model;

import java.util.Locale;

/**
 * 钱包冻结类型
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum WalletFreezeType {
    /** 支付预占冻结 */
    PAY_HOLD,
    /** 提现冻结 */
    WITHDRAW_HOLD,
    /** 风控冻结 */
    RISK_HOLD,
    /** 退款在途冻结 */
    REFUND_HOLD,
    /** 争议冻结 */
    DISPUTE_HOLD,
    /** 结算冻结 */
    SETTLEMENT_HOLD,
    /** 欠款冻结 */
    DEBT_HOLD,
    /** 合规冻结 */
    COMPLIANCE_HOLD,
    /** 营销冻结 */
    PROMO_HOLD,
    /** 系统保护冻结 */
    SYSTEM_GUARD_HOLD,
    /** 历史迁移冻结 */
    LEGACY_MIGRATION;

    /** 默认冻结类型 */
    public static final WalletFreezeType DEFAULT = PAY_HOLD;

    /**
     * 处理业务数据。
     */
    public static WalletFreezeType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT;
        }
        return WalletFreezeType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
