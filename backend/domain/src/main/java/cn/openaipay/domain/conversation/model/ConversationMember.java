package cn.openaipay.domain.conversation.model;

import java.time.LocalDateTime;

/**
 * 会话成员模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class ConversationMember {

    /** 主键ID。 */
    private final Long id;
    /** 会话号。 */
    private final String conversationNo;
    /** 用户ID。 */
    private final Long userId;
    /** 对端用户ID。 */
    private final Long peerUserId;
    /** 未读消息数。 */
    private long unreadCount;
    /** 最后已读消息ID。 */
    private String lastReadMessageId;
    /** 最后已读时间。 */
    private LocalDateTime lastReadAt;
    /** 是否免打扰。 */
    private boolean mute;
    /** 是否置顶。 */
    private boolean pin;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public ConversationMember(Long id,
                              String conversationNo,
                              Long userId,
                              Long peerUserId,
                              long unreadCount,
                              String lastReadMessageId,
                              LocalDateTime lastReadAt,
                              boolean mute,
                              boolean pin,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.conversationNo = normalizeRequired(conversationNo, "conversationNo");
        this.userId = requirePositive(userId, "userId");
        this.peerUserId = requirePositive(peerUserId, "peerUserId");
        this.unreadCount = Math.max(0, unreadCount);
        this.lastReadMessageId = normalizeOptional(lastReadMessageId);
        this.lastReadAt = lastReadAt;
        this.mute = mute;
        this.pin = pin;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static ConversationMember create(String conversationNo,
                                            Long userId,
                                            Long peerUserId,
                                            LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new ConversationMember(
                null,
                conversationNo,
                userId,
                peerUserId,
                0,
                null,
                null,
                false,
                false,
                createdAt,
                createdAt
        );
    }

    /**
     * 处理业务数据。
     */
    public void increaseUnread(LocalDateTime now) {
        this.unreadCount = this.unreadCount + 1;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 标记业务数据。
     */
    public void markRead(String lastReadMessageId, LocalDateTime now) {
        this.unreadCount = 0;
        this.lastReadMessageId = normalizeOptional(lastReadMessageId);
        this.lastReadAt = now == null ? LocalDateTime.now() : now;
        this.updatedAt = this.lastReadAt;
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
     * 获取用户ID。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取用户ID。
     */
    public Long getPeerUserId() {
        return peerUserId;
    }

    /**
     * 获取数量信息。
     */
    public long getUnreadCount() {
        return unreadCount;
    }

    /**
     * 获取消息ID。
     */
    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isMute() {
        return mute;
    }

    /**
     * 判断是否PIN信息。
     */
    public boolean isPin() {
        return pin;
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

    private static Long requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return value;
    }
}
