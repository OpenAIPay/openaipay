package cn.openaipay.domain.trade.model;

import java.util.Locale;

/**
 * 基金业务交易类型。
 *
 * 业务场景：爱存申购、普通转出、快速转出、收益结转虽然都归属基金域，但账单展示和清算含义不同。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum TradeFundTradeType {
    /** 申购基金。 */
    PURCHASE,
    /** 普通赎回。 */
    REDEEM,
    /** 快速赎回。 */
    FAST_REDEEM,
    /** 转入基金账户。 */
    TRANSFER_IN,
    /** 转出基金账户。 */
    TRANSFER_OUT,
    /** 收益结转。 */
    YIELD_SETTLE,
    /** 支付占用冻结。 */
    PAY_FREEZE;

    /**
     * 处理业务数据。
     */
    public static TradeFundTradeType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("fundTradeType must not be blank");
        }
        try {
            return TradeFundTradeType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported fundTradeType: " + raw);
        }
    }
}
