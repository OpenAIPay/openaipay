package cn.openaipay.application.fundaccount.dto;

import cn.openaipay.domain.shared.number.FundAmount;
import java.math.BigDecimal;
/**
 * 基金账户数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundAccountDTO(
        /** 用户ID */
        Long userId,
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
        /** 业务金额 */
        FundAmount holdingAmount,
        /** 收益信息 */
        FundAmount accumulatedIncome,
        /** 收益信息 */
        FundAmount yesterdayIncome,
        /** latestNAV信息 */
        FundAmount latestNav,
        /** 业务状态 */
        String accountStatus,
        /** 业务费率 */
        BigDecimal annualizedYieldRate,
        /** 收益PER10K信息 */
        FundAmount incomePer10k
) {
}
