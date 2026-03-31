package cn.openaipay.domain.pricing.model;

import java.util.Locale;

/**
 * PricingFee模式枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PricingFeeMode {
    /**
      * 按费率计算手续费。
       */
    RATE,
    /**
      * 固定面额策略。
       */
    FIXED,
    /**
      * 费率与固定费叠加计算手续费。
       */
    RATE_PLUS_FIXED;

    /**
     * 处理业务数据。
     */
    public static PricingFeeMode from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("feeMode must not be blank");
        }
        try {
            return PricingFeeMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported feeMode: " + raw);
        }
    }
}
