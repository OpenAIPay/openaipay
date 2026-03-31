package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 后台管理登录请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record AdminLoginRequest(
        /** 用户名 */
        @NotBlank(message = "不能为空") String username,
        /** 登录密码 */
        @NotBlank(message = "不能为空") String password,
        /** 设备ID */
        String deviceId
) {
}
