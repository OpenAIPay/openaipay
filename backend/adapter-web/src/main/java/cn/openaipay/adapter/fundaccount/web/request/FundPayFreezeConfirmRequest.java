package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 基金支付冻结确认请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record FundPayFreezeConfirmRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 资金交易单号 */
        @NotBlank String fundTradeNo
) {
}

