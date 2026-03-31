package cn.openaipay.domain.accounting.model;

/**
 * 会计事件查询条件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingEventQuery(
        /** 事件ID */
        String eventId,
        /** 事件类型 */
        String eventType,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 业务单号 */
        String bizOrderNo,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 状态编码 */
        AccountingEventStatus status,
        /** 查询条数上限 */
        Integer limit
) {
}
