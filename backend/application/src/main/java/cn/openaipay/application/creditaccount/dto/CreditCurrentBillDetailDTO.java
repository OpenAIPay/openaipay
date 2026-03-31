package cn.openaipay.application.creditaccount.dto;

import java.util.List;
import org.joda.money.Money;

/**
 * 爱花当前账单明细页查询结果。
 *
 * 业务场景：爱花首页点击“总计账单-明细”后进入白底账单详情页，
 * 页面顶部摘要和第一页可见的账单列表统一读取该对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record CreditCurrentBillDetailDTO(
        /** 标题 */
        String title,
        /** 入账周期文案，例如“入账周期：2月1日 - 2月28日”。 */
        String periodText,
        /** 当前账单尚待归还金额，展示在页面顶部大额数字位。 */
        Money dueAmount,
        /** 当前账单总计金额，展示在摘要区“3月账单总计”一行。 */
        Money statementTotalAmount,
        /** 当前账单退款总额，展示在摘要区“退款”一行。 */
        Money refundedAmount,
        /** 当前账单已还金额，展示在摘要区“已还款”一行。 */
        Money repaidAmount,
        /** 条目列表 */
        List<CreditCurrentBillDetailItemDTO> items
) {
}
