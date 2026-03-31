package cn.openaipay.domain.creditaccount.service;

import org.joda.money.Money;

/**
 * 信用账单明细行。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record CreditBillDetailItem(
        /** 日期信息 */
        String dateText,
        /** 展示标题 */
        String displayTitle,
        /** 展示副标题 */
        String displaySubtitle,
        /** 金额 */
        Money amount,
        /** 业务单号 */
        String businessNo
) {
}
