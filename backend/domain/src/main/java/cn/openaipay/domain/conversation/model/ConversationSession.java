package cn.openaipay.domain.conversation.model;

import java.time.LocalDateTime;

/**
 * 会话模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class ConversationSession {

    /** 主键ID。 */
    private final Long id;
    /** 会话号。 */
    private final String conversationNo;
    /** 会话类型。 */
    private final ConversationType conversationType;
    /** 业务唯一键。 */
    private final String bizKey;
    /** 最后一条消息ID。 */
    private String lastMessageId;
    /** 最后一条消息预览。 */
    private String lastMessagePreview;
    /** 最后消息时间。 */
    private LocalDateTime lastMessageAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public ConversationSession(Long id,
                               String conversationNo,
                               ConversationType conversationType,
                               String bizKey,
                               String lastMessageId,
                               String lastMessagePreview,
                               LocalDateTime lastMessageAt,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt) {
        this.id = id;
        this.conversationNo = normalizeRequired(conversationNo, "conversationNo");
        this.conversationType = conversationType == null ? ConversationType.PRIVATE : conversationType;
        this.bizKey = normalizeRequired(bizKey, "bizKey");
        this.lastMessageId = normalizeOptional(lastMessageId);
        this.lastMessagePreview = normalizeOptional(lastMessagePreview);
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 处理会话信息。
     */
    public static ConversationSession privateConversation(String conversationNo,
                                                          String bizKey,
                                                          LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new ConversationSession(
                null,
                conversationNo,
                ConversationType.PRIVATE,
                bizKey,
                null,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 更新消息信息。
     */
    public void updateLastMessage(String messageId, String preview, LocalDateTime messageAt, LocalDateTime now) {
        this.lastMessageId = normalizeRequired(messageId, "messageId");
        this.lastMessagePreview = normalizeOptional(preview);
        this.lastMessageAt = messageAt == null ? LocalDateTime.now() : messageAt;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取会话NO信息。
     */
    public String getConversationNo() {
        return conversationNo;
    }

    /**
     * 获取会话类型信息。
     */
    public ConversationType getConversationType() {
        return conversationType;
    }

    /**
     * 获取业务KEY信息。
     */
    public String getBizKey() {
        return bizKey;
    }

    /**
     * 获取消息ID。
     */
    public String getLastMessageId() {
        return lastMessageId;
    }

    /**
     * 获取消息信息。
     */
    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    /**
     * 获取消息AT信息。
     */
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
