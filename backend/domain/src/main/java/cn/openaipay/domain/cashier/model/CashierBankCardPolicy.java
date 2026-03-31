package cn.openaipay.domain.cashier.model;

import java.util.Locale;

/**
 * 收银台银行卡准入策略，约束某个场景下银行卡渠道可以使用借记卡、信用卡，还是两者都可用。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public enum CashierBankCardPolicy {
    /**
     * 不限制银行卡类型，付款类场景可同时展示借记卡与信用卡。
     */
    ALL_CARDS,
    /**
     * 仅允许借记卡，适用于充值、提现、账户转入转出、还款到账等必须走储蓄卡的场景。
     */
    DEBIT_ONLY,
    /**
     * 仅允许信用卡，预留给未来需要强制信用支付的场景。
     */
    CREDIT_ONLY;

    /**
     * 判断当前策略下某张银行卡是否允许出现在收银台。
     *
     * @param rawCardType 银行卡类型原始值，例如 DEBIT / CREDIT
     * @return true 表示该银行卡符合当前场景准入策略
     */
    public boolean supports(String rawCardType) {
        String normalizedCardType = normalizeCardType(rawCardType);
        return switch (this) {
            case ALL_CARDS -> true;
            case DEBIT_ONLY -> normalizedCardType.isEmpty() || "DEBIT".equals(normalizedCardType);
            case CREDIT_ONLY -> "CREDIT".equals(normalizedCardType);
        };
    }

    private String normalizeCardType(String rawCardType) {
        if (rawCardType == null || rawCardType.isBlank()) {
            return "";
        }
        return rawCardType.trim().toUpperCase(Locale.ROOT);
    }
}
