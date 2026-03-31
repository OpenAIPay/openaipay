package cn.openaipay.domain.user.model;

import java.util.Locale;

/**
 * 用户能力开通项。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum UserFeatureCode {

    /** 已开通爱存基金账户。 */
    FUND_ACCOUNT_OPENED,
    /** 已开通爱花。 */
    AICREDIT_OPENED,
    /** 已开通爱借。 */
    AILOAN_OPENED;

    /**
     * 解析能力编码。
     */
    public static UserFeatureCode fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("featureCode must not be blank");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "FUND_ACCOUNT_OPENED" -> FUND_ACCOUNT_OPENED;
            case "AICREDIT_OPENED" -> AICREDIT_OPENED;
            case "AILOAN_OPENED" -> AILOAN_OPENED;
            default -> throw new IllegalArgumentException("unsupported featureCode: " + raw);
        };
    }

    /**
     * 查询数据库使用标准能力编码。
     */
    public String persistentCode() {
        return name();
    }
}
