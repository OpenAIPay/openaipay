package cn.openaipay.application.user.dto;
/**
 * 用户初始化数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UserInitDTO(
        /** 资料信息 */
        UserProfileDTO profile,
        /** 安全信息 */
        UserSecurityDTO security,
        /** 设置项 */
        UserSettingsDTO settings
) {
}
