package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * 应用版本状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppVersionStatus {
    /** 草稿。 */
    DRAFT,
    /** 已启用。 */
    ENABLED,
    /** 已停用。 */
    DISABLED,
    /** 已归档。 */
    ARCHIVED;

    /**
     * 处理编码。
     */
    public static AppVersionStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return DRAFT;
        }
        try {
            return AppVersionStatus.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appVersionStatus: " + code);
        }
    }
}
