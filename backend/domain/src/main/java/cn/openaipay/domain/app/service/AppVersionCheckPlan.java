package cn.openaipay.domain.app.service;

/**
 * 版本检查领域结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AppVersionCheckPlan(
        /** 版本信息 */
        boolean versionPromptEnabled,
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
        String minSupportedVersionNo
) {
    /**
     * 处理业务数据。
     */
    public static AppVersionCheckPlan empty(boolean versionPromptEnabled) {
        return new AppVersionCheckPlan(
                versionPromptEnabled,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                null
        );
    }
}
