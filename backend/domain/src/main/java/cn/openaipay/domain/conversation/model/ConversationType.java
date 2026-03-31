package cn.openaipay.domain.conversation.model;

/**
 * 会话类型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public enum ConversationType {
    /** 单聊。 */
    PRIVATE;

    /**
     * 处理业务数据。
     */
    public static ConversationType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return PRIVATE;
        }
        return ConversationType.valueOf(raw.trim().toUpperCase());
    }
}
