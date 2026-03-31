package cn.openaipay.adapter.auth.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 本机号码验证登录请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/28
 */
public record MobileVerifyLoginRequest(
        /** 登录账号ID */
        @NotBlank(message = "不能为空") String loginId,
        /** 设备ID */
        @NotBlank(message = "不能为空") String deviceId
) {
}
