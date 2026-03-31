package cn.openaipay.domain.coupon.model;

import java.util.Locale;

/**
 * 优惠券发放状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CouponIssueStatus {
    /**
      * 红包已发放未使用，可在有效期内核销。
       */
    UNUSED,
    /**
      * 红包已核销，不能再次使用。
       */
    USED,
    /**
      * 红包已过期，不可继续使用。
       */
    EXPIRED,
    /**
      * 冻结状态，暂不可执行关键操作。
       */
    FROZEN;

    /**
     * 处理业务数据。
     */
    public static CouponIssueStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("issue status must not be blank");
        }
        try {
            return CouponIssueStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported issue status: " + raw);
        }
    }
}
