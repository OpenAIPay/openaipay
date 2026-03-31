package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金切换Confirm请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSwitchConfirmRequest(
        /** 订单号 */
        @NotBlank String orderNo,
        /** 来源NAV信息 */
        @DecimalMin(value = "0.0001") FundAmount sourceNav,
        /** 目标NAV信息 */
        @DecimalMin(value = "0.0001") FundAmount targetNav
) {
}
