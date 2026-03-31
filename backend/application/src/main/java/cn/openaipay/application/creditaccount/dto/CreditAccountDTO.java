package cn.openaipay.application.creditaccount.dto;

import org.joda.money.Money;

/**
 * 爱花信用账户查询结果。
 *
 * 业务场景：爱花首页、还款页、总计账单页统一读取该对象，
 * 既展示当前待还金额，也展示本月新增消费累计到下月账单的动态金额。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CreditAccountDTO(
        /** 爱花信用账户编号，供还款链路和账单页跳转时定位账户。 */
        String accountNo,
        /** 用户ID */
        Long userId,
        /** 爱花授信总额度，展示在爱花首页“总计额度”区域。 */
        Money totalLimit,
        /** 可用额度 */
        Money availableLimit,
        /** 本金余额 */
        Money principalBalance,
        /** 逾期本金余额 */
        Money overduePrincipalBalance,
        /** 利息余额 */
        Money interestBalance,
        /** 罚息余额 */
        Money fineBalance,
        /** 本月新增爱花消费累计金额，会在总计账单页展示为下月账单累计值。 */
        Money nextMonthBillAccumulatedAmount,
        /** 爱花每月固定还款日，展示在爱花首页与还款提示中。 */
        Integer repayDayOfMonth,
        /** 爱花账户状态，控制是否允许继续消费或还款。 */
        String accountStatus,
        /** 爱花支付状态，控制账单是否可正常还款。 */
        String payStatus
) {
}
