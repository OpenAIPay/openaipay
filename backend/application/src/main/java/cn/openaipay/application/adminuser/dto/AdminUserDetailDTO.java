package cn.openaipay.application.adminuser.dto;

import cn.openaipay.domain.shared.number.FundAmount;
import org.joda.money.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminUserDetailDTO(
        /** 账户摘要 */
        AdminUserSummaryDTO account,
        /** 头像地址 */
        String avatarUrl,
        /** 国家编码 */
        String countryCode,
        /** 脱敏实名 */
        String maskedRealName,
        /** 证件号 */
        String idCardNo,
        /** 性别 */
        String gender,
        /** 地区 */
        String region,
        /** 生日 */
        LocalDate birthday,
        /** 生物识别开关 */
        Boolean biometricEnabled,
        /** 二次验证模式 */
        String twoFactorMode,
        /** 风险等级 */
        String riskLevel,
        /** 设备锁开关 */
        Boolean deviceLockEnabled,
        /** 隐私模式开关 */
        Boolean privacyModeEnabled,
        /** 是否允许手机号搜索 */
        Boolean allowSearchByMobile,
        /** 是否允许UID搜索 */
        Boolean allowSearchByAipayUid,
        /** 是否隐藏实名 */
        Boolean hideRealName,
        /** 个性化推荐开关 */
        Boolean personalizedRecommendationEnabled,
        /** 信用利息余额 */
        Money creditInterestBalance,
        /** 信用罚息余额 */
        Money creditFineBalance,
        /** 基金可用总份额 */
        FundAmount fundTotalAvailableShare,
        /** 基金累计总收益 */
        FundAmount fundTotalAccumulatedIncome,
        /** 基金账户快照 */
        List<AdminFundAccountSnapshotDTO> fundAccounts,
        /** 资料更新时间 */
        LocalDateTime profileUpdatedAt
) {
}
