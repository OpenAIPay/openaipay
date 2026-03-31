package cn.openaipay.domain.pricing.model;

import java.util.Locale;

/**
 * PricingFeeBearer枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PricingFeeBearer {
    /**
      * 手续费由付款方承担。
       */
    PAYER,
    /**
      * 手续费由收款方承担。
       */
    PAYEE,
    /**
      * 手续费由平台承担。
       */
    PLATFORM;

    /**
     * 处理业务数据。
     */
    public static PricingFeeBearer from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("feeBearer must not be blank");
        }
        try {
            return PricingFeeBearer.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported feeBearer: " + raw);
        }
    }
}
