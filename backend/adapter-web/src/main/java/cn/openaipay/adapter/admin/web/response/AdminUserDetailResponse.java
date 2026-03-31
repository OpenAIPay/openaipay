package cn.openaipay.adapter.admin.web.response;

import cn.openaipay.domain.shared.number.FundAmount;
import org.joda.money.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情响应模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record AdminUserDetailResponse(
        /** 账户信息 */
        AdminUserSummaryResponse account,
        /** 头像地址 */
        String avatarUrl,
        /** 业务编码 */
        String countryCode,
        /** 业务名称 */
        String maskedRealName,
        /** ID卡单号 */
        String idCardNo,
        /** 性别 */
        String gender,
        /** 地区 */
        String region,
        /** 生日 */
        LocalDate birthday,
        /** 生物识别开关 */
        Boolean biometricEnabled,
        /** TWOfactormode信息 */
        String twoFactorMode,
        /** 风控信息 */
        String riskLevel,
        /** 设备信息 */
        Boolean deviceLockEnabled,
        /** 隐私模式开关 */
        Boolean privacyModeEnabled,
        /** BY手机号 */
        Boolean allowSearchByMobile,
        /** BYUID */
        Boolean allowSearchByAipayUid,
        /** 业务名称 */
        Boolean hideRealName,
        /** 个性化推荐开关 */
        Boolean personalizedRecommendationEnabled,
        /** 信用信息 */
        Money creditInterestBalance,
        /** 信用信息 */
        Money creditFineBalance,
        /** 资金总信息 */
        FundAmount fundTotalAvailableShare,
        /** 资金总收益信息 */
        FundAmount fundTotalAccumulatedIncome,
        /** 资金信息 */
        List<AdminFundAccountSnapshotResponse> fundAccounts,
        /** 资料更新时间 */
        LocalDateTime profileUpdatedAt
) {
}
