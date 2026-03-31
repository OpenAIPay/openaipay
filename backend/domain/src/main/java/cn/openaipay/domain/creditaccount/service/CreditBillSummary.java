package cn.openaipay.domain.creditaccount.service;

import java.util.List;
import org.joda.money.Money;

/**
 * 信用账单摘要。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record CreditBillSummary(
        /** 标题 */
        String title,
        /** 账期文案 */
        String periodText,
        /** DUE金额 */
        Money dueAmount,
        /** 总金额 */
        Money statementTotalAmount,
        /** 业务金额 */
        Money refundedAmount,
        /** 业务金额 */
        Money repaidAmount,
        /** 条目列表 */
        List<CreditBillDetailItem> items
) {
}
