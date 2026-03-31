package cn.openaipay.domain.feedback.model;

import java.util.Locale;

/**
 * 反馈类型枚举。
 *
 * @author: tenggk.ai
 * @date: 2026/03/11
 */
public enum FeedbackType {

    /** 产品建议。 */
    PRODUCT_SUGGESTION,
    /** 服务投诉。 */
    SERVICE_COMPLAINT,
    /** 功能异常。 */
    FUNCTION_EXCEPTION,
    /** 其他类型。 */
    OTHER;

    /**
     * 处理编码。
     */
    public static FeedbackType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return PRODUCT_SUGGESTION;
        }
        try {
            return FeedbackType.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported feedbackType: " + code);
        }
    }
}
