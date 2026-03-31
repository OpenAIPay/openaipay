package cn.openaipay.domain.riskpolicy.model;

import java.util.Locale;

/**
 * 风控场景编码。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public enum RiskSceneCode {
    LOAN_DRAW,
    LOAN_REPAY,
    FUND_SUBSCRIBE,
    FUND_REDEEM,
    FUND_FAST_REDEEM,
    FUND_SWITCH,
    FUND_PAY_FREEZE;

    /**
     * 从字符串转换场景编码。
     */
    public static RiskSceneCode from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("risk scene code must not be blank");
        }
        try {
            return RiskSceneCode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported risk scene code: " + raw);
        }
    }
}
