package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金收益Settle请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundIncomeSettleRequest(
        /** 订单号 */
        @NotBlank String orderNo,
        /** 用户ID */
        @NotNull Long userId,
        /** 资金编码 */
        String fundCode,
        /** 收益金额 */
        @NotNull @DecimalMin(value = "0.0001") FundAmount incomeAmount,
        /** NAV信息 */
        @DecimalMin(value = "0.0001") FundAmount nav,
        /** 业务单号 */
        String businessNo
) {
}
