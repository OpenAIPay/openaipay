package cn.openaipay.adapter.admin.web.response;

import cn.openaipay.domain.shared.number.FundAmount;

import java.time.LocalDateTime;

/**
 * 用户基金账户快照响应模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record AdminFundAccountSnapshotResponse(
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode,
        /** 可用份额 */
        FundAmount availableShare,
        /** 冻结份额 */
        FundAmount frozenShare,
        /** 业务金额 */
        FundAmount pendingSubscribeAmount,
        /** 待赎回份额 */
        FundAmount pendingRedeemShare,
        /** 收益信息 */
        FundAmount accumulatedIncome,
        /** 收益信息 */
        FundAmount yesterdayIncome,
        /** latestNAV信息 */
        FundAmount latestNav,
        /** 业务状态 */
        String accountStatus,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
