package cn.openaipay.application.loanaccount.dto;

import org.joda.money.Money;

/**
 * 爱借账户账本快照。
 *
 * @author: tenggk.ai
 * @date: 2026/03/28
 */
public record LoanAccountLedgerDTO(
        /** 爱借账户号。 */
        String accountNo,
        /** 用户ID。 */
        Long userId,
        /** 总额度。 */
        Money totalLimit,
        /** 可用额度。 */
        Money availableLimit,
        /** 每月还款日。 */
        Integer repayDayOfMonth,
        /** 账户状态。 */
        String accountStatus,
        /** 支付状态。 */
        String payStatus
) {
}
