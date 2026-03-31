package cn.openaipay.domain.outbound.model;

/**
 * 出金订单状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum OutboundStatus {
    /** 已创建。 */
    CREATED,
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
    public static OutboundStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return CREATED;
        }
        return OutboundStatus.valueOf(raw.trim().toUpperCase());
    }
}
