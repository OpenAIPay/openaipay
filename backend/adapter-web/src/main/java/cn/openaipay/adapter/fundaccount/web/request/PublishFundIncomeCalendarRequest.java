package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * Publish基金收益日历请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record PublishFundIncomeCalendarRequest(
        /** 资金编码 */
        @NotBlank String fundCode,
        /** 业务日期 */
        String bizDate,
        /** NAV信息 */
        @NotNull @DecimalMin(value = "0.0001") FundAmount nav,
        /** 收益PER10K信息 */
        @NotNull @DecimalMin(value = "0.0000") FundAmount incomePer10k
) {
}
