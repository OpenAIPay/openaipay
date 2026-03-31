package cn.openaipay.domain.coupon.model;

import java.util.Locale;

/**
 * 优惠券模板状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CouponTemplateStatus {
    /**
      * 草稿状态，仅可在后台继续编辑。
       */
    DRAFT,
    /**
      * 已启用状态，可参与线上业务流程。
       */
    ACTIVE,
    /**
      * 暂停状态，暂不参与线上流程。
       */
    PAUSED,
    /**
      * 红包已过期，不可继续使用。
       */
    EXPIRED;

    /**
     * 处理业务数据。
     */
    public static CouponTemplateStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("template status must not be blank");
        }
        try {
            return CouponTemplateStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported template status: " + raw);
        }
    }
}
