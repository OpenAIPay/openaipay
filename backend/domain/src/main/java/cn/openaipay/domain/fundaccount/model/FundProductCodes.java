package cn.openaipay.domain.fundaccount.model;

import java.util.Locale;

/**
 * 基金产品编码常量。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public final class FundProductCodes {

    /** 爱存主产品编码。 */
    public static final String AICASH = "AICASH";
    /** 默认主产品编码。 */
    public static final String DEFAULT_FUND_CODE = AICASH;

    private FundProductCodes() {
    }

    /**
     * 标准化并在空值时返回默认基金编码。
     */
    public static String normalizeOrDefault(String fundCode) {
        String normalized = normalizeNullable(fundCode);
        return normalized == null ? DEFAULT_FUND_CODE : normalized;
    }

    /**
     * 标准化基金编码，空值返回 null。
     */
    public static String normalizeNullable(String fundCode) {
        if (fundCode == null) {
            return null;
        }
        String trimmed = fundCode.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case AICASH -> AICASH;
            default -> normalized;
        };
    }

    /**
     * 判断是否为爱存主产品编码。
     */
    public static boolean isPrimaryFundCode(String fundCode) {
        return AICASH.equals(normalizeOrDefault(fundCode));
    }
}
