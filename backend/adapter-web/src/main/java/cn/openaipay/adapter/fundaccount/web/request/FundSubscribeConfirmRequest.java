package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金申购Confirm请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSubscribeConfirmRequest(
        /** 订单号 */
        @NotBlank String orderNo,
        /** 确认份额 */
        @DecimalMin(value = "0.0001") FundAmount confirmedShare,
        /** NAV信息 */
        @DecimalMin(value = "0.0001") FundAmount nav
) {
}
