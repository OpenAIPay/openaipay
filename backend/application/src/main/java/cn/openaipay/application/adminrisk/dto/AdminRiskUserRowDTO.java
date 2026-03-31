package cn.openaipay.application.adminrisk.dto;

import java.time.LocalDateTime;

/**
 * 风控用户行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminRiskUserRowDTO(
        /** 用户ID */
        Long userId,
        /** 展示名称 */
        String displayName,
        /** 爱付UID */
        String aipayUid,
        /** 登录账号 */
        String loginId,
        /** 手机号 */
        String mobile,
        /** 账号状态 */
        String accountStatus,
        /** KYC级别 */
        String kycLevel,
        /** 风险等级 */
        String riskLevel,
        /** 二次验证模式 */
        String twoFactorMode,
        /** 设备锁开关 */
        Boolean deviceLockEnabled,
        /** 隐私模式开关 */
        Boolean privacyModeEnabled,
        /** 允许手机号搜索 */
        Boolean allowSearchByMobile,
        /** 允许UID搜索 */
        Boolean allowSearchByAipayUid,
        /** 隐藏实名 */
        Boolean hideRealName,
        /** 个性化推荐开关 */
        Boolean personalizedRecommendationEnabled,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
