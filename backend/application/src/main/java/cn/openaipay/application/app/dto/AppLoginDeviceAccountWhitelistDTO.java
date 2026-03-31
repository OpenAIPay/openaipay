package cn.openaipay.application.app.dto;

/**
 * 登录设备白名单账号 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
public record AppLoginDeviceAccountWhitelistDTO(
        /** 登录账号 */
        String loginId,
        /** 昵称 */
        String nickname
) {
}
