package cn.openaipay.domain.inbound.model;

import java.util.Locale;

/**
 * 入金订单状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum InboundStatus {
    /** 初始化。 */
    INIT,
    /** 已提交渠道。 */
    SUBMITTED,
    /** 渠道已受理。 */
    ACCEPTED,
    /** 结果待确认。 */
    RECON_PENDING,
    /** 处理成功。 */
    SUCCEEDED,
    /** 处理失败。 */
    FAILED,
    /** 已取消。 */
    CANCELED;

    /**
     * 处理业务数据。
     */
    public static InboundStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("inboundStatus must not be blank");
        }
        try {
            return InboundStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported inboundStatus: " + raw);
        }
    }
}
