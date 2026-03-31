package cn.openaipay.application.app.dto;

/**
 * 演示账号初始化配置 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/28
 */
public record AppDemoProvisioningConfigDTO(
        /** 演示模板登录号 */
        String demoTemplateLoginId,
        /** 演示联系人登录号 */
        String demoContactLoginId,
        /** 演示注册默认密码 */
        String demoLoginPassword
) {
}

