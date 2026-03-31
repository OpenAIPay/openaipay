package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * 更新提示频率。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppUpdatePromptFrequency {
    /** 每次都提示。 */
    ALWAYS,
    /** 每天提示一次。 */
    DAILY,
    /** 每个版本提示一次。 */
    ONCE_PER_VERSION,
    /** 静默不提示。 */
    SILENT;

    /**
     * 处理编码。
     */
    public static AppUpdatePromptFrequency fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ONCE_PER_VERSION;
        }
        try {
            return AppUpdatePromptFrequency.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appUpdatePromptFrequency: " + code);
        }
    }
}
