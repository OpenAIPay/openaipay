package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 新增或更新基金产品请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpsertFundProductRequest(
        /** 资金编码 */
        @NotBlank String fundCode,
        /** 产品名称 */
        String productName,
        /** 币种编码 */
        String currencyCode,
        /** 产品状态 */
        String productStatus,
        /** MIN金额 */
        @DecimalMin(value = "0.0001") FundAmount singleSubscribeMinAmount,
        /** 最大金额 */
        @DecimalMin(value = "0.0001") FundAmount singleSubscribeMaxAmount,
        /** 最大金额 */
        @DecimalMin(value = "0.0001") FundAmount dailySubscribeMaxAmount,
        /** single赎回MIN份额信息 */
        @DecimalMin(value = "0.0001") FundAmount singleRedeemMinShare,
        /** 最大信息 */
        @DecimalMin(value = "0.0001") FundAmount singleRedeemMaxShare,
        /** 最大信息 */
        @DecimalMin(value = "0.0001") FundAmount dailyRedeemMaxShare,
        /** 快速额度信息 */
        @DecimalMin(value = "0.0001") FundAmount fastRedeemDailyQuota,
        /** 快速赎回PER用户单日额度信息 */
        @DecimalMin(value = "0.0001") FundAmount fastRedeemPerUserDailyQuota,
        /** 开关启用标记 */
        Boolean switchEnabled
) {
}
