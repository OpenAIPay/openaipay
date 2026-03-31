package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * 版本更新类型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppUpdateType {
    /** 可选更新。 */
    OPTIONAL,
    /** 推荐更新。 */
    RECOMMENDED,
    /** 强制更新。 */
    FORCE;

    /**
     * 处理编码。
     */
    public static AppUpdateType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OPTIONAL;
        }
        try {
            return AppUpdateType.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appUpdateType: " + code);
        }
    }
}
