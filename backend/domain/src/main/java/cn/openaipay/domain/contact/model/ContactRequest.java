package cn.openaipay.domain.contact.model;

import java.time.LocalDateTime;

/**
 * 好友申请模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class ContactRequest {

    /** 主键ID。 */
    private final Long id;
    /** 请求号。 */
    private final String requestNo;
    /** 申请发起方用户ID。 */
    private final Long requesterUserId;
    /** 目标用户ID。 */
    private final Long targetUserId;
    /** 申请附言。 */
    private final String applyMessage;
    /** 当前状态。 */
    private ContactRequestStatus status;
    /** 处理人用户ID。 */
    private Long handledByUserId;
    /** 处理时间。 */
    private LocalDateTime handledAt;
    /** 创建时间。 */
    private final LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public ContactRequest(Long id,
                          String requestNo,
                          Long requesterUserId,
                          Long targetUserId,
                          String applyMessage,
                          ContactRequestStatus status,
                          Long handledByUserId,
                          LocalDateTime handledAt,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.id = id;
        this.requestNo = normalizeRequired(requestNo, "requestNo");
        this.requesterUserId = requirePositive(requesterUserId, "requesterUserId");
        this.targetUserId = requirePositive(targetUserId, "targetUserId");
        this.applyMessage = normalizeOptional(applyMessage);
        this.status = status == null ? ContactRequestStatus.PENDING : status;
        this.handledByUserId = handledByUserId;
        this.handledAt = handledAt;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static ContactRequest create(String requestNo,
                                        Long requesterUserId,
                                        Long targetUserId,
                                        String applyMessage,
                                        LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new ContactRequest(
                null,
                requestNo,
                requesterUserId,
                targetUserId,
                applyMessage,
                ContactRequestStatus.PENDING,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 处理业务数据。
     */
    public void accept(Long handledByUserId, LocalDateTime now) {
        ensurePending();
        this.status = ContactRequestStatus.ACCEPTED;
        this.handledByUserId = requirePositive(handledByUserId, "handledByUserId");
        this.handledAt = now == null ? LocalDateTime.now() : now;
        this.updatedAt = this.handledAt;
    }

    /**
     * 处理业务数据。
     */
    public void reject(Long handledByUserId, LocalDateTime now) {
        ensurePending();
        this.status = ContactRequestStatus.REJECTED;
        this.handledByUserId = requirePositive(handledByUserId, "handledByUserId");
        this.handledAt = now == null ? LocalDateTime.now() : now;
        this.updatedAt = this.handledAt;
    }

    /**
     * 取消业务数据。
     */
    public void cancel(Long handledByUserId, LocalDateTime now) {
        ensurePending();
        this.status = ContactRequestStatus.CANCELED;
        this.handledByUserId = requirePositive(handledByUserId, "handledByUserId");
        this.handledAt = now == null ? LocalDateTime.now() : now;
        this.updatedAt = this.handledAt;
    }

    private void ensurePending() {
        if (this.status != ContactRequestStatus.PENDING) {
            throw new IllegalStateException("contact request status must be PENDING");
        }
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取请求NO信息。
     */
    public String getRequestNo() {
        return requestNo;
    }

    /**
     * 获取用户ID。
     */
    public Long getRequesterUserId() {
        return requesterUserId;
    }

    /**
     * 获取目标用户ID。
     */
    public Long getTargetUserId() {
        return targetUserId;
    }

    /**
     * 获取消息信息。
     */
    public String getApplyMessage() {
        return applyMessage;
    }

    /**
     * 获取状态。
     */
    public ContactRequestStatus getStatus() {
        return status;
    }

    /**
     * 按用户ID获取记录。
     */
    public Long getHandledByUserId() {
        return handledByUserId;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getHandledAt() {
        return handledAt;
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
