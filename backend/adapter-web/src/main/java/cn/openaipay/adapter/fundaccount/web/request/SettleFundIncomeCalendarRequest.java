package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * Settle基金收益日历请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record SettleFundIncomeCalendarRequest(
        /** 资金编码 */
        @NotBlank String fundCode,
        /** 业务日期 */
        String bizDate
) {
}
