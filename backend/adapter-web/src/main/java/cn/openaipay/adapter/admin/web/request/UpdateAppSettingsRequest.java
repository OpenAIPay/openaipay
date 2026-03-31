package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新应用设置请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record UpdateAppSettingsRequest(
        /** 版本信息 */
        @NotNull(message = "不能为空") Boolean versionPromptEnabled,
        /** 演示账号自动登录开关 */
        @NotNull(message = "不能为空") Boolean demoAutoLoginEnabled,
        /** 登录本机注册校验开关 */
        @NotNull(message = "不能为空") Boolean loginDeviceBindingCheckEnabled,
        /** 演示模板登录号 */
        @Size(max = 32, message = "长度不能超过32") String demoTemplateLoginId,
        /** 演示联系人登录号 */
        @Size(max = 32, message = "长度不能超过32") String demoContactLoginId,
        /** 演示注册默认密码 */
        @Size(max = 64, message = "长度不能超过64") String demoLoginPassword
) {
}
