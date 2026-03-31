package cn.openaipay.domain.coupon.model;

import java.util.Locale;

/**
 * 优惠券Scene类型枚举
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum CouponSceneType {
    /**
      * 社交红包场景。
       */
    SOCIAL_GIFT,
    /**
      * 拉新获客场景。
       */
    NEW_USER_ACQUISITION,
    /**
      * 用户活跃激励场景。
       */
    USER_ACTIVATION,
    /**
      * 支付激励补贴场景。
       */
    PAYMENT_INCENTIVE,
    /**
      * 商户联合营销场景。
       */
    MERCHANT_MARKETING,
    /**
      * 节日活动投放场景。
       */
    FESTIVAL_CAMPAIGN,
    /**
      * 企业福利发放场景。
       */
    ENTERPRISE_GRANT,
    /**
      * 服务补偿发放场景。
       */
    SERVICE_COMPENSATION;

    /**
     * 处理业务数据。
     */
    public static CouponSceneType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("sceneType must not be blank");
        }
        try {
            return CouponSceneType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported sceneType: " + raw);
        }
    }
}
