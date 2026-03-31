package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 基金赎回Cancel请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundRedeemCancelRequest(
        /** 订单号 */
        @NotBlank String orderNo
) {
}
