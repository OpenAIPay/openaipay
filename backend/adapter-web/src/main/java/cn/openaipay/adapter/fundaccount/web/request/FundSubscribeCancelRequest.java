package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 基金申购Cancel请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSubscribeCancelRequest(
        /** 订单号 */
        @NotBlank String orderNo
) {
}
