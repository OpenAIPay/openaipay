package cn.openaipay.domain.outbox.model;

import java.util.Locale;

/**
 * 标准 Outbox 消息状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum OutboxMessageStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    DEAD;

    /**
     * 处理业务数据。
     */
    public static OutboxMessageStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("outbox message status must not be blank");
        }
        try {
            return OutboxMessageStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported outbox message status: " + raw);
        }
    }
}
