package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;

/**
 * 应用定义 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record AppInfoDTO(
        /** 应用编码 */
        String appCode,
        /** 应用名称 */
        String appName,
        /** 状态编码 */
        String status,
        /** 版本信息 */
        boolean versionPromptEnabled,
        /** 演示账号自动登录开关 */
        boolean demoAutoLoginEnabled,
        /** 登录本机注册校验开关 */
        boolean loginDeviceBindingCheckEnabled,
        /** 演示模板登录号 */
        String demoTemplateLoginId,
        /** 演示联系人登录号 */
        String demoContactLoginId,
        /** 演示注册默认密码是否已配置 */
        boolean demoLoginPasswordConfigured,
        /** 演示注册默认密码掩码 */
        String demoLoginPasswordMasked,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
