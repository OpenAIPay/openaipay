package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 基金支付冻结请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record FundPayFreezeRequest(
        /** 资金交易单号 */
        @NotBlank String fundTradeNo,
        /** 用户ID */
        @NotNull Long userId,
        /** 资金编码 */
        String fundCode,
        /** 金额 */
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        /** 币种编码 */
        String currencyCode,
        /** 业务单号 */
        String businessNo
) {
}
