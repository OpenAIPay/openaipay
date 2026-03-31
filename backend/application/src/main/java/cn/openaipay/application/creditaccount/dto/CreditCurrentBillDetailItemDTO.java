package cn.openaipay.application.creditaccount.dto;

import org.joda.money.Money;

/**
 * 爱花当前账单明细项。
 *
 * 业务场景：爱花“3月总计账单”明细页按可见列表渲染每一笔账单消费，
 * 该对象用于向 iOS 页面返回日期、文案、金额和业务流水号。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record CreditCurrentBillDetailItemDTO(
        /** 账单明细页展示的日期文案，例如“2月27日”。 */
        String dateText,
        /** 账单消费主标题，展示在明细列表每一行的第一行。 */
        String displayTitle,
        /** 账单消费副标题，展示在明细列表每一行的第二行。 */
        String displaySubtitle,
        /** 金额 */
        Money amount,
        /** 业务流水号，供排查账单映射和后续扩展商户信息使用。 */
        String businessNo
) {
}
