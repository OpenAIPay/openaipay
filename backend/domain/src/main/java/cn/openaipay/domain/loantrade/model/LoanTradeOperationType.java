package cn.openaipay.domain.loantrade.model;

import java.util.Locale;

/**
 * 爱借交易操作类型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum LoanTradeOperationType {
    LEND,
    REPAY;

    /**
     * 处理业务数据。
     */
    public static LoanTradeOperationType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("loan trade operationType must not be blank");
        }
        try {
            return LoanTradeOperationType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported loan trade operationType: " + raw);
        }
    }
}
