package cn.openaipay.application.loanaccount.command;

import org.joda.money.Money;

/**
 * 新增借贷账户命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record CreateLoanAccountCommand(
        /** 用户ID */
        Long userId,
        /** 业务单号 */
        String accountNo,
        /** 总信息 */
        Money totalLimit,
        /** repayDAYOFmonth信息 */
        Integer repayDayOfMonth
) {
}
