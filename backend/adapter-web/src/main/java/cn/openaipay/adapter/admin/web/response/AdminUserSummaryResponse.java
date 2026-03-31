package cn.openaipay.adapter.admin.web.response;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 用户摘要响应模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record AdminUserSummaryResponse(
        /** 用户ID */
        String userId,
        /** 业务名称 */
        String realName,
        /** 爱支付UID */
        String aipayUid,
        /** 登录账号ID */
        String loginId,
        /** 昵称 */
        String nickname,
        /** 手机号 */
        String mobile,
        /** 业务状态 */
        String accountStatus,
        /** KYClevel信息 */
        String kycLevel,
        /** 登录密码SET信息 */
        Boolean loginPasswordSet,
        /** 支付密码SET信息 */
        Boolean payPasswordSet,
        /** 钱包状态 */
        String walletAccountStatus,
        /** 钱包信息 */
        Money walletAvailableBalance,
        /** 钱包信息 */
        Money walletReservedBalance,
        /** 信用状态 */
        String creditAccountStatus,
        /** 信用总信息 */
        Money creditTotalLimit,
        /** 信用金额 */
        Money creditUsedAmount,
        /** 信用信息 */
        Money creditPrincipalBalance,
        /** 基金账户数量 */
        Integer fundAccountCount,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
