package cn.openaipay.application.user.dto;
/**
 * 用户Settings数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UserSettingsDTO(
        /** 用户ID */
        Long userId,
        /** BY手机号 */
        boolean allowSearchByMobile,
        /** BYUID */
        boolean allowSearchByAipayUid,
        /** 业务名称 */
        boolean hideRealName,
        /** 个性化推荐开关 */
        boolean personalizedRecommendationEnabled,
        /** 生物识别开关 */
        boolean biometricEnabled,
        /** TWOfactormode信息 */
        String twoFactorMode,
        /** 风控信息 */
        String riskLevel,
        /** 设备信息 */
        boolean deviceLockEnabled,
        /** 隐私模式开关 */
        boolean privacyModeEnabled
) {
}
