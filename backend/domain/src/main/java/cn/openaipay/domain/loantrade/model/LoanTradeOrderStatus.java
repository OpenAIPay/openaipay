package cn.openaipay.domain.loantrade.model;

import java.util.Locale;

/**
 * 爱借交易单状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum LoanTradeOrderStatus {
    TRIED,
    CONFIRMED,
    CANCELED;

    /**
     * 处理业务数据。
     */
    public static LoanTradeOrderStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("loan trade status must not be blank");
        }
        try {
            return LoanTradeOrderStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported loan trade status: " + raw);
        }
    }
}
