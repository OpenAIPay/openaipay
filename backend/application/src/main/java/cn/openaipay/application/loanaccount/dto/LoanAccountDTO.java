package cn.openaipay.application.loanaccount.dto;

import java.math.BigDecimal;
import org.joda.money.Money;

/**
 * LoanAccountDTO 数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/06
 */
public record LoanAccountDTO(
        /** 业务单号 */
        String accountNo,
        /** 用户ID */
        Long userId,
        /** 总信息 */
        Money totalLimit,
        /** 可用额度 */
        Money availableLimit,
        /** 业务费率 */
        BigDecimal annualRate,
        /** 原始费率 */
        BigDecimal originalAnnualRate,
        /** repayDAYOFmonth信息 */
        Integer repayDayOfMonth,
        /** 业务状态 */
        String accountStatus,
        /** 支付状态 */
        String payStatus
) {
}
