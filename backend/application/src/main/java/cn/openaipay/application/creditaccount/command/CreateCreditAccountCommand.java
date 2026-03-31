package cn.openaipay.application.creditaccount.command;

import org.joda.money.Money;
/**
 * 创建信用账户命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateCreditAccountCommand(
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
