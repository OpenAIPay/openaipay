package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.NotNull;
/**
 * 创建基金账户请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateFundAccountRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode
) {
}
