package cn.openaipay.domain.coupon.model;

import java.util.Locale;

/**
 * 优惠券Value类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CouponValueType {
    /**
      * 固定面额策略。
       */
    FIXED,
    /**
      * 随机面额策略。
       */
    RANDOM;

    /**
     * 处理业务数据。
     */
    public static CouponValueType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("valueType must not be blank");
        }
        try {
            return CouponValueType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported valueType: " + raw);
        }
    }
}
