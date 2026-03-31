package cn.openaipay.domain.trade.model;

import java.util.Locale;

/**
 * 信用业务交易类型。
 *
 * 业务场景：同为信用产品，消费、全额还款、最低还款、借款支用的业务语义完全不同，需要单独编码。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum TradeCreditTradeType {
    /** 爱花消费入账。 */
    CONSUME,
    /** 常规信用还款。 */
    REPAY,
    /** 最低还款。 */
    MINIMUM_REPAY,
    /** 全额还款。 */
    FULL_REPAY,
    /** 爱借放款。 */
    LOAN_DRAW,
    /** 爱借还款。 */
    LOAN_REPAY,
    /** 信用产品服务费扣收。 */
    FEE_CHARGE,
    /** 信用产品利息结转。 */
    INTEREST_SETTLE;

    /**
     * 处理业务数据。
     */
    public static TradeCreditTradeType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("creditTradeType must not be blank");
        }
        try {
            return TradeCreditTradeType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported creditTradeType: " + raw);
        }
    }
}
