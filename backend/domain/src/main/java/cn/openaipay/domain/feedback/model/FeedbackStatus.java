package cn.openaipay.domain.feedback.model;

import java.util.Locale;

/**
 * 反馈单状态枚举。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public enum FeedbackStatus {

    /** 已提交。 */
    SUBMITTED,
    /** 处理中。 */
    PROCESSING,
    /** 已解决。 */
    RESOLVED,
    /** 已拒绝。 */
    REJECTED,
    /** 已关闭。 */
    CLOSED;

    /**
     * 处理编码。
     */
    public static FeedbackStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SUBMITTED;
        }
        try {
            return FeedbackStatus.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported feedbackStatus: " + code);
        }
    }
}
