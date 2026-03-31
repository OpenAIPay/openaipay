package cn.openaipay.domain.trade.model;

import java.util.Locale;

/**
 * 交易类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum TradeType {
    /**
      * 入金类交易。
       */
    DEPOSIT,
    /**
      * 提现类交易。
       */
    WITHDRAW,
    /**
      * 支付类交易。
       */
    PAY,
    /**
      * 转账类交易。
       */
    TRANSFER,
    /**
      * 退款类交易。
       */
    REFUND;

    /**
     * 处理业务数据。
     */
    public static TradeType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("tradeType must not be blank");
        }
        try {
            return TradeType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported tradeType: " + raw);
        }
    }
}
