package cn.openaipay.application.adminfund.dto;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * 授信账户行
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminCreditAccountRowDTO(
        /** 产品编码 */
        String productCode,
        /** 用户ID */
        Long userId,
        /** 用户展示名称 */
        String userDisplayName,
        /** 爱付UID */
        String aipayUid,
        /** 账户号 */
        String accountNo,
        /** 总额度 */
        Money totalLimit,
        /** 可用额度 */
        Money availableLimit,
        /** 本金余额 */
        Money principalBalance,
        /** 本金未出账金额 */
        Money principalUnreachAmount,
        /** 逾期本金余额 */
        Money overduePrincipalBalance,
        /** 逾期本金未出账金额 */
        Money overduePrincipalUnreachAmount,
        /** 利息余额 */
        Money interestBalance,
        /** 罚息余额 */
        Money fineBalance,
        /** 账号状态 */
        String accountStatus,
        /** 支付状态 */
        String payStatus,
        /** 还款日 */
        Integer repayDayOfMonth,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
