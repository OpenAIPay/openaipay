package cn.openaipay.domain.message.model;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 聊天消息模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class ChatMessage {

    /** 主键ID。 */
    private final Long id;
    /** 消息ID。 */
    private final String messageId;
    /** 会话号。 */
    private final String conversationNo;
    /** 发送方用户ID。 */
    private final Long senderUserId;
    /** 接收方用户ID。 */
    private final Long receiverUserId;
    /** 消息类型。 */
    private final MessageType messageType;
    /** 文本内容。 */
    private final String contentText;
    /** 媒体资源ID。 */
    private final String mediaId;
    /** 金额。 */
    private final Money amount;
    /** 交易号。 */
    private final String tradeOrderNo;
    /** 扩展载荷。 */
    private String extPayload;
    /** 消息状态。 */
    private MessageStatus messageStatus;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public ChatMessage(Long id,
                       String messageId,
                       String conversationNo,
                       Long senderUserId,
                       Long receiverUserId,
                       MessageType messageType,
                       String contentText,
                       String mediaId,
                       Money amount,
                       String tradeOrderNo,
                       String extPayload,
                       MessageStatus messageStatus,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.messageId = normalizeRequired(messageId, "messageId");
        this.conversationNo = normalizeRequired(conversationNo, "conversationNo");
        this.senderUserId = requirePositive(senderUserId, "senderUserId");
        this.receiverUserId = requirePositive(receiverUserId, "receiverUserId");
        this.messageType = messageType == null ? MessageType.TEXT : messageType;
        this.contentText = normalizeOptional(contentText);
        this.mediaId = normalizeOptional(mediaId);
        this.amount = amount;
        this.tradeOrderNo = normalizeOptional(tradeOrderNo);
        this.extPayload = normalizeOptional(extPayload);
        this.messageStatus = messageStatus == null ? MessageStatus.SENT : messageStatus;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static ChatMessage create(String messageId,
                                     String conversationNo,
                                     Long senderUserId,
                                     Long receiverUserId,
                                     MessageType messageType,
                                     String contentText,
                                     String mediaId,
                                     Money amount,
                                     String tradeOrderNo,
                                     String extPayload,
                                     LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new ChatMessage(
                null,
                messageId,
                conversationNo,
                senderUserId,
                receiverUserId,
                messageType,
                contentText,
                mediaId,
                amount,
                tradeOrderNo,
                extPayload,
                MessageStatus.SENT,
                createdAt,
                createdAt
        );
    }

    /**
     * 处理业务数据。
     */
    public void recall(LocalDateTime now) {
        this.messageStatus = MessageStatus.RECALLED;
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 更新扩展载荷。
     */
    public void updateExtPayload(String extPayload, LocalDateTime now) {
        this.extPayload = normalizeOptional(extPayload);
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取消息ID。
     */
    public String getMessageId() {
        return messageId;
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
    public Long getSenderUserId() {
        return senderUserId;
    }

    /**
     * 获取用户ID。
     */
    public Long getReceiverUserId() {
        return receiverUserId;
    }

    /**
     * 获取消息类型信息。
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * 获取内容信息。
     */
    public String getContentText() {
        return contentText;
    }

    /**
     * 获取媒体ID。
     */
    public String getMediaId() {
        return mediaId;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取交易订单NO信息。
     */
    public String getTradeOrderNo() {
        return tradeOrderNo;
    }

    /**
     * 获取EXT信息。
     */
    public String getExtPayload() {
        return extPayload;
    }

    /**
     * 获取消息状态。
     */
    public MessageStatus getMessageStatus() {
        return messageStatus;
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
