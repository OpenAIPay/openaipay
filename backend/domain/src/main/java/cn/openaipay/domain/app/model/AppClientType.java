package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * App 客户端类型。
 *
 * 当前版本管理第一版只覆盖 iPhone 端，因此默认值为 IOS_IPHONE。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppClientType {
    /** iPhone 客户端。 */
    IOS_IPHONE;

    /**
     * 处理编码。
     */
    public static AppClientType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return IOS_IPHONE;
        }
        try {
            return AppClientType.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appClientType: " + code);
        }
    }
}
