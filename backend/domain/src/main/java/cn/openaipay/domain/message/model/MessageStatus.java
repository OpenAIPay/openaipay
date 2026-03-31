package cn.openaipay.domain.message.model;

/**
 * 消息状态
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum MessageStatus {
    /** 已发送。 */
    SENT,
    /** 已撤回。 */
    RECALLED;

    /**
     * 处理业务数据。
     */
    public static MessageStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return SENT;
        }
        return MessageStatus.valueOf(raw.trim().toUpperCase());
    }
}
