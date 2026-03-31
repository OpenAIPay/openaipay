package cn.openaipay.domain.outbox.model;

import java.time.LocalDateTime;

/**
 * 标准 Outbox 消息模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public class OutboxMessage {
    /** 数据库主键ID */
    private final Long id;
    /** 消息主题 */
    private final String topic;
    /** 消息唯一键 */
    private final String messageKey;
    /** 消息载荷内容 */
    private final String payload;
    /** 当前状态编码 */
    private OutboxMessageStatus status;
    /** 当前重试次数 */
    private int retryCount;
    /** 最大重试次数 */
    private final int maxRetryCount;
    /** 下次重试时间 */
    private LocalDateTime nextRetryAt;
    /** 处理开始时间 */
    private LocalDateTime processingStartedAt;
    /** 最近一次失败原因 */
    private String lastError;
    /** 记录创建时间 */
    private final LocalDateTime createdAt;
    /** 记录更新时间 */
    private LocalDateTime updatedAt;

    public OutboxMessage(Long id,
                         String topic,
                         String messageKey,
                         String payload,
                         OutboxMessageStatus status,
                         int retryCount,
                         int maxRetryCount,
                         LocalDateTime nextRetryAt,
                         LocalDateTime processingStartedAt,
                         String lastError,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.topic = normalizeRequired(topic, "topic");
        this.messageKey = normalizeRequired(messageKey, "messageKey");
        this.payload = normalizeRequired(payload, "payload");
        this.status = status == null ? OutboxMessageStatus.PENDING : status;
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must be greater than or equal to 0");
        }
        if (maxRetryCount <= 0) {
            throw new IllegalArgumentException("maxRetryCount must be greater than 0");
        }
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.nextRetryAt = nextRetryAt == null ? LocalDateTime.now() : nextRetryAt;
        this.processingStartedAt = processingStartedAt;
        this.lastError = normalizeOptional(lastError);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    /**
     * 创建业务数据。
     */
    public static OutboxMessage createPending(String topic,
                                              String messageKey,
                                              String payload,
                                              int maxRetryCount,
                                              LocalDateTime now) {
        LocalDateTime createdAt = now == null ? LocalDateTime.now() : now;
        return new OutboxMessage(
                null,
                topic,
                messageKey,
                payload,
                OutboxMessageStatus.PENDING,
                0,
                maxRetryCount,
                createdAt,
                null,
                null,
                createdAt,
                createdAt
        );
    }

    /**
     * 标记业务数据。
     */
    public void markProcessing(LocalDateTime now) {
        this.status = OutboxMessageStatus.PROCESSING;
        this.processingStartedAt = now == null ? LocalDateTime.now() : now;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markSucceeded(LocalDateTime now) {
        this.status = OutboxMessageStatus.SUCCEEDED;
        this.processingStartedAt = null;
        this.lastError = null;
        touch(now);
    }

    /**
     * 标记重试信息。
     */
    public void markRetry(String errorMessage, LocalDateTime nextRetryAt, LocalDateTime now) {
        this.status = OutboxMessageStatus.PENDING;
        this.retryCount = this.retryCount + 1;
        this.processingStartedAt = null;
        this.lastError = normalizeOptional(errorMessage);
        this.nextRetryAt = nextRetryAt == null ? LocalDateTime.now() : nextRetryAt;
        touch(now);
    }

    /**
     * 标记业务数据。
     */
    public void markDead(String errorMessage, LocalDateTime now) {
        this.status = OutboxMessageStatus.DEAD;
        this.retryCount = this.retryCount + 1;
        this.processingStartedAt = null;
        this.lastError = normalizeOptional(errorMessage);
        touch(now);
    }

    /**
     * 判断是否可重试信息。
     */
    public boolean canRetry() {
        return this.retryCount + 1 < this.maxRetryCount;
    }

    private void touch(LocalDateTime now) {
        this.updatedAt = now == null ? LocalDateTime.now() : now;
    }

    private String normalizeRequired(String raw, String field) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取主题信息。
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 获取消息KEY信息。
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * 获取业务数据。
     */
    public String getPayload() {
        return payload;
    }

    /**
     * 获取状态。
     */
    public OutboxMessageStatus getStatus() {
        return status;
    }

    /**
     * 获取重试数量信息。
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 获取MAX重试数量信息。
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 获取重试AT信息。
     */
    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    /**
     * 获取错误信息。
     */
    public String getLastError() {
        return lastError;
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
}
