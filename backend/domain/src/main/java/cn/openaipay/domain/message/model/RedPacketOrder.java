package cn.openaipay.domain.message.model;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 聊天红包订单。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class RedPacketOrder {

    /** 主键ID。 */
    private final Long id;
    /** 红包单号。 */
    private final String redPacketNo;
    /** 关联消息ID。 */
    private final String messageId;
    /** 会话号。 */
    private final String conversationNo;
    /** 发红包用户ID。 */
    private final Long senderUserId;
    /** 收红包用户ID。 */
    private final Long receiverUserId;
    /** 红包中间账户用户ID。 */
    private final Long holdingUserId;
    /** 红包金额。 */
    private final Money amount;
    /** 发出时资金交易号。 */
    private final String fundingTradeNo;
    /** 领取时资金交易号。 */
    private String claimTradeNo;
    /** 付款方式。 */
    private final String paymentMethod;
    /** 封面ID。 */
    private final String coverId;
    /** 封面标题。 */
    private final String coverTitle;
    /** 祝福语。 */
    private final String blessingText;
    /** 红包状态。 */
    private RedPacketOrderStatus status;
    /** 领取时间。 */
    private LocalDateTime claimedAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public RedPacketOrder(Long id,
                          String redPacketNo,
                          String messageId,
                          String conversationNo,
                          Long senderUserId,
                          Long receiverUserId,
                          Long holdingUserId,
                          Money amount,
                          String fundingTradeNo,
                          String claimTradeNo,
                          String paymentMethod,
                          String coverId,
                          String coverTitle,
                          String blessingText,
                          RedPacketOrderStatus status,
                          LocalDateTime claimedAt,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.redPacketNo = normalizeRequired(redPacketNo, "redPacketNo");
        this.messageId = normalizeRequired(messageId, "messageId");
        this.conversationNo = normalizeRequired(conversationNo, "conversationNo");
        this.senderUserId = requirePositive(senderUserId, "senderUserId");
        this.receiverUserId = requirePositive(receiverUserId, "receiverUserId");
        this.holdingUserId = requirePositive(holdingUserId, "holdingUserId");
        this.amount = requirePositiveAmount(amount, "amount");
        this.fundingTradeNo = normalizeRequired(fundingTradeNo, "fundingTradeNo");
        this.claimTradeNo = normalizeOptional(claimTradeNo);
        this.paymentMethod = normalizeRequired(paymentMethod, "paymentMethod");
        this.coverId = normalizeOptional(coverId);
        this.coverTitle = normalizeOptional(coverTitle);
        this.blessingText = normalizeOptional(blessingText);
        this.status = status == null ? RedPacketOrderStatus.PENDING_CLAIM : status;
        this.claimedAt = claimedAt;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建待领取红包订单。
     */
    public static RedPacketOrder createPending(String redPacketNo,
                                               String messageId,
                                               String conversationNo,
                                               Long senderUserId,
                                               Long receiverUserId,
                                               Long holdingUserId,
                                               Money amount,
                                               String fundingTradeNo,
                                               String paymentMethod,
                                               String coverId,
                                               String coverTitle,
                                               String blessingText,
                                               LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new RedPacketOrder(
                null,
                redPacketNo,
                messageId,
                conversationNo,
                senderUserId,
                receiverUserId,
                holdingUserId,
                amount,
                fundingTradeNo,
                null,
                paymentMethod,
                coverId,
                coverTitle,
                blessingText,
                RedPacketOrderStatus.PENDING_CLAIM,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 标记红包已领取。
     */
    public void markClaimed(Long claimantUserId, String claimTradeNo, LocalDateTime now) {
        Long normalizedClaimant = requirePositive(claimantUserId, "claimantUserId");
        if (!receiverUserId.equals(normalizedClaimant)) {
            throw new IllegalStateException("current user is not the red packet receiver");
        }
        if (status == RedPacketOrderStatus.CLAIMED) {
            return;
        }
        if (status != RedPacketOrderStatus.PENDING_CLAIM) {
            throw new IllegalStateException("red packet status does not allow claim: " + status.name());
        }
        this.claimTradeNo = normalizeRequired(claimTradeNo, "claimTradeNo");
        this.status = RedPacketOrderStatus.CLAIMED;
        this.claimedAt = now == null ? LocalDateTime.now() : now;
        this.updatedAt = this.claimedAt;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取RED红包NO信息。
     */
    public String getRedPacketNo() {
        return redPacketNo;
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
     * 获取用户ID。
     */
    public Long getHoldingUserId() {
        return holdingUserId;
    }

    /**
     * 获取金额。
     */
    public Money getAmount() {
        return amount;
    }

    /**
     * 获取资金交易NO信息。
     */
    public String getFundingTradeNo() {
        return fundingTradeNo;
    }

    /**
     * 获取交易NO信息。
     */
    public String getClaimTradeNo() {
        return claimTradeNo;
    }

    /**
     * 获取业务数据。
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * 获取ID。
     */
    public String getCoverId() {
        return coverId;
    }

    /**
     * 获取业务数据。
     */
    public String getCoverTitle() {
        return coverTitle;
    }

    /**
     * 获取业务数据。
     */
    public String getBlessingText() {
        return blessingText;
    }

    /**
     * 获取状态。
     */
    public RedPacketOrderStatus getStatus() {
        return status;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getClaimedAt() {
        return claimedAt;
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

    private static Money requirePositiveAmount(Money amount, String fieldName) {
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
        return amount;
    }
}
