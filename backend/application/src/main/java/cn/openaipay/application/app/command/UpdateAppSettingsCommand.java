package cn.openaipay.application.app.command;

/**
 * 更新应用设置命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record UpdateAppSettingsCommand(
        /** 应用编码 */
        String appCode,
        /** 版本信息 */
        Boolean versionPromptEnabled,
        /** 演示账号自动登录开关 */
        Boolean demoAutoLoginEnabled,
        /** 登录本机注册校验开关 */
        Boolean loginDeviceBindingCheckEnabled,
        /** 演示模板登录号 */
        String demoTemplateLoginId,
        /** 演示联系人登录号 */
        String demoContactLoginId,
        /** 演示注册默认密码 */
        String demoLoginPassword
) {
}
