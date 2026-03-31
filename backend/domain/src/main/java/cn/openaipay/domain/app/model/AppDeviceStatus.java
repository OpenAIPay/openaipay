package cn.openaipay.domain.app.model;

import java.util.Locale;

/**
 * App 设备状态。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum AppDeviceStatus {
    /** 已安装。 */
    INSTALLED,
    /** 活跃中。 */
    ACTIVE,
    /** 暂未活跃。 */
    INACTIVE,
    /** 已卸载。 */
    UNINSTALLED;

    /**
     * 处理编码。
     */
    public static AppDeviceStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return INSTALLED;
        }
        try {
            return AppDeviceStatus.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported appDeviceStatus: " + code);
        }
    }
}
