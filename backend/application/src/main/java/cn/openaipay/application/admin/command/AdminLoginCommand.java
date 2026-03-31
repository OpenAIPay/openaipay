package cn.openaipay.application.admin.command;
/**
 * 后台管理登录命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminLoginCommand(
        /** 用户名 */
        String username,
        /** 登录密码 */
        String password,
        /** 设备ID */
        String deviceId
) {
}
