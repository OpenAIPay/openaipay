package cn.openaipay.application.fundaccount.command;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * Publish基金收益日历命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record PublishFundIncomeCalendarCommand(
        /** 资金编码 */
        String fundCode,
        /** 业务日期 */
        String bizDate,
        /** NAV信息 */
        FundAmount nav,
        /** 收益PER10K信息 */
        FundAmount incomePer10k
) {
}
