package cn.openaipay.domain.message.model;

import java.util.Locale;

/**
 * 红包订单状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public enum RedPacketOrderStatus {

    /** 待领取。 */
    PENDING_CLAIM,
    /** 已领取。 */
    CLAIMED,
    /** 已过期。 */
    EXPIRED,
    /** 已退回。 */
    REFUNDED;

    /**
     * 按文本解析状态，无法识别时默认待领取。
     */
    public static RedPacketOrderStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return PENDING_CLAIM;
        }
        return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
