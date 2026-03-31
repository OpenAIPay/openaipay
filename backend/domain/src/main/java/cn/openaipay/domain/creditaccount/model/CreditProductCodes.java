package cn.openaipay.domain.creditaccount.model;

import java.util.List;
import java.util.Locale;

/**
 * 信用产品编码常量。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public final class CreditProductCodes {

    /** 爱花主产品编码。 */
    public static final String AICREDIT = "AICREDIT";
    /** 爱借主产品编码。 */
    public static final String AILOAN = "AILOAN";
    private CreditProductCodes() {
    }

    /**
     * 标准化信用产品编码，空值返回 null。
     */
    public static String normalizeNullable(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case AICREDIT -> AICREDIT;
            case AILOAN -> AILOAN;
            default -> normalized;
        };
    }

    /**
     * 标准化信用产品编码，空值返回爱花。
     */
    public static String normalizeOrDefault(String raw) {
        String normalized = normalizeNullable(raw);
        return normalized == null ? AICREDIT : normalized;
    }

    /**
     * 判断是否为爱花编码。
     */
    public static boolean isAiCredit(String raw) {
        return AICREDIT.equals(normalizeOrDefault(raw));
    }

    /**
     * 判断是否为爱借编码。
     */
    public static boolean isAiLoan(String raw) {
        return AILOAN.equals(normalizeOrDefault(raw));
    }

    /**
     * 查询数据库编码。
     */
    public static List<String> compatibleCodes(String raw) {
        String normalized = normalizeOrDefault(raw);
        return List.of(normalized);
    }
}
