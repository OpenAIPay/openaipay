package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * iOS 包发布状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppReleaseStatus {
    /** 草稿。 */
    DRAFT,
    /** 审核中。 */
    REVIEWING,
    /** 已发布。 */
    PUBLISHED,
    /** 已驳回。 */
    REJECTED,
    /** 已下线。 */
    OFFLINE;

    /**
     * 处理编码。
     */
    public static AppReleaseStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return DRAFT;
        }
        try {
            return AppReleaseStatus.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appReleaseStatus: " + code);
        }
    }
}
