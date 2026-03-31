package cn.openaipay.application.fundaccount.dto;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金产品数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundProductDTO(
        /** 资金编码 */
        String fundCode,
        /** 产品名称 */
        String productName,
        /** 币种编码 */
        String currencyCode,
        /** 产品状态 */
        String productStatus,
        /** MIN金额 */
        FundAmount singleSubscribeMinAmount,
        /** 最大金额 */
        FundAmount singleSubscribeMaxAmount,
        /** 最大金额 */
        FundAmount dailySubscribeMaxAmount,
        /** single赎回MIN份额信息 */
        FundAmount singleRedeemMinShare,
        /** 最大信息 */
        FundAmount singleRedeemMaxShare,
        /** 最大信息 */
        FundAmount dailyRedeemMaxShare,
        /** 快速额度信息 */
        FundAmount fastRedeemDailyQuota,
        /** 快速赎回PER用户单日额度信息 */
        FundAmount fastRedeemPerUserDailyQuota,
        /** 开关启用标记 */
        boolean switchEnabled
) {
}
