package cn.openaipay.domain.agreement.model;

import java.util.Locale;

/**
 * 协议业务类型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public enum AgreementBizType {

    /** 基金账户开通签约。 */
    FUND_ACCOUNT_OPEN,
    /** 爱花开通签约。 */
    AICREDIT_OPEN,
    /** 爱借开通签约。 */
    AILOAN_OPEN;

    /**
     * 解析业务类型。
     *
     * @param raw 原始编码
     * @return 枚举
     */
    public static AgreementBizType fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("agreement bizType must not be blank");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "FUND_ACCOUNT_OPEN" -> FUND_ACCOUNT_OPEN;
            case "AICREDIT_OPEN" -> AICREDIT_OPEN;
            case "AILOAN_OPEN" -> AILOAN_OPEN;
            default -> throw new IllegalArgumentException("unsupported agreement bizType: " + raw);
        };
    }

    /**
     * 查询数据库使用标准业务编码。
     */
    public String persistentCode() {
        return name();
    }
}
