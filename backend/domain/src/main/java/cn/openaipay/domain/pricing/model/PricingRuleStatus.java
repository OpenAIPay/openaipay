package cn.openaipay.domain.pricing.model;

import java.util.Locale;

/**
 * Pricing规则状态枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum PricingRuleStatus {
    /**
      * 草稿状态，仅可在后台继续编辑。
       */
    DRAFT,
    /**
      * 已启用状态，可参与线上业务流程。
       */
    ACTIVE,
    /**
      * 停用状态，不再参与规则匹配。
       */
    INACTIVE;

    /**
     * 处理业务数据。
     */
    public static PricingRuleStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
        try {
            return PricingRuleStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported status: " + raw);
        }
    }
}
