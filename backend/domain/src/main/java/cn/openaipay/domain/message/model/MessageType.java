package cn.openaipay.domain.message.model;

/**
 * 消息类型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum MessageType {
    /** 文本消息。 */
    TEXT,
    /** 图片消息。 */
    IMAGE,
    /** 转账消息。 */
    TRANSFER,
    /** 红包消息。 */
    RED_PACKET,
    /** 系统消息。 */
    SYSTEM;

    /**
     * 处理业务数据。
     */
    public static MessageType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return TEXT;
        }
        return MessageType.valueOf(raw.trim().toUpperCase());
    }
}
