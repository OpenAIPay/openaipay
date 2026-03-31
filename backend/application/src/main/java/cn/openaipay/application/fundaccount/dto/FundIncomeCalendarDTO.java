package cn.openaipay.application.fundaccount.dto;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金收益日历数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundIncomeCalendarDTO(
        /** 资金编码 */
        String fundCode,
        /** 业务日期 */
        String bizDate,
        /** NAV信息 */
        FundAmount nav,
        /** 收益PER10K信息 */
        FundAmount incomePer10k,
        /** 业务状态 */
        String calendarStatus
) {
}
