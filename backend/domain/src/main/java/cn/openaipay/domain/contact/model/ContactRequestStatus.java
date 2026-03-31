package cn.openaipay.domain.contact.model;

/**
 * 好友申请状态
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum ContactRequestStatus {
    /** 待处理。 */
    PENDING,
    /** 已接受。 */
    ACCEPTED,
    /** 已拒绝。 */
    REJECTED,
    /** 已取消。 */
    CANCELED;

    /**
     * 处理业务数据。
     */
    public static ContactRequestStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return PENDING;
        }
        return ContactRequestStatus.valueOf(raw.trim().toUpperCase());
    }
}
