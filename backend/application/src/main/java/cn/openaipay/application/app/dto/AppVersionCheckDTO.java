package cn.openaipay.application.app.dto;

/**
 * 客户端版本检查 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record AppVersionCheckDTO(
        /** 应用编码 */
        String appCode,
        /** 当前版本号 */
        String currentVersionNo,
        /** 版本信息 */
        boolean versionPromptEnabled,
        /** 演示账号自动登录开关 */
        boolean demoAutoLoginEnabled,
        /** 版本编码 */
        String latestVersionCode,
        /** 版本单号 */
        String latestVersionNo,
        /** 可更新标记 */
        boolean updateAvailable,
        /** 强制更新标记 */
        boolean forceUpdate,
        /** 业务类型 */
        String updateType,
        /** 更新提示频率 */
        String updatePromptFrequency,
        /** 版本信息 */
        String versionDescription,
        /** MIN版本单号 */
        String minSupportedVersionNo,
        /** 应用地址 */
        String appStoreUrl,
        /** 安装包大小字节数 */
        Long packageSizeBytes,
        /** MD5信息 */
        String md5,
        /** 业务状态 */
        String releaseStatus
) {
}
