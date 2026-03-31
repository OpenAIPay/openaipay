package cn.openaipay.application.fundaccount.command;
/**
 * Settle基金收益日历命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record SettleFundIncomeCalendarCommand(
        /** 资金编码 */
        String fundCode,
        /** 业务日期 */
        String bizDate
) {
}
