package cn.openaipay.application.adminuser.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 用户摘要
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminUserSummaryDTO(
        /** 用户ID */
        String userId,
        /** 真实姓名 */
        String realName,
        /** 爱付UID */
        String aipayUid,
        /** 登录账号 */
        String loginId,
        /** 昵称 */
        String nickname,
        /** 手机号 */
        String mobile,
        /** 账号状态 */
        String accountStatus,
        /** KYC级别 */
        String kycLevel,
        /** 登录密码是否已设置 */
        Boolean loginPasswordSet,
        /** 支付密码是否已设置 */
        Boolean payPasswordSet,
        /** 钱包状态 */
        String walletAccountStatus,
        /** 钱包可用余额 */
        Money walletAvailableBalance,
        /** 钱包冻结余额 */
        Money walletReservedBalance,
        /** 信用账户状态 */
        String creditAccountStatus,
        /** 信用总额度 */
        Money creditTotalLimit,
        /** 已使用额度 */
        Money creditUsedAmount,
        /** 信用本金余额 */
        Money creditPrincipalBalance,
        /** 基金账户数 */
        Integer fundAccountCount,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
