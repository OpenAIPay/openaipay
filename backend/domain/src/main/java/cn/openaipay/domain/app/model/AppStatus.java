package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * App 通用启停状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppStatus {
    /** 已启用。 */
    ENABLED,
    /** 已禁用。 */
    DISABLED;

    /**
     * 处理编码。
     */
    public static AppStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ENABLED;
        }
        try {
            return AppStatus.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appStatus: " + code);
        }
    }
}
