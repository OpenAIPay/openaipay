package cn.openaipay.application.adminfund.dto;

import cn.openaipay.domain.shared.number.FundAmount;
import java.time.LocalDateTime;

/**
 * 基金账户行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminFundAccountRowDTO(
        /** 用户ID */
        Long userId,
        /** 用户展示名称 */
        String userDisplayName,
        /** 爱付UID */
        String aipayUid,
        /** 基金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode,
        /** 可用份额 */
        FundAmount availableShare,
        /** 冻结份额 */
        FundAmount frozenShare,
        /** 待申购金额 */
        FundAmount pendingSubscribeAmount,
        /** 待赎回份额 */
        FundAmount pendingRedeemShare,
        /** 累计收益 */
        FundAmount accumulatedIncome,
        /** 昨日收益 */
        FundAmount yesterdayIncome,
        /** 最新净值 */
        FundAmount latestNav,
        /** 账号状态 */
        String accountStatus,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
