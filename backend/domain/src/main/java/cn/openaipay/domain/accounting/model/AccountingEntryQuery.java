package cn.openaipay.domain.accounting.model;

/**
 * 会计分录查询条件。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingEntryQuery(
        /** 业务单号 */
        String voucherNo,
        /** 科目编码 */
        String subjectCode,
        /** 所属类型 */
        String ownerType,
        /** 所属ID */
        Long ownerId,
        /** 业务单号 */
        String bizOrderNo,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 查询条数上限 */
        Integer limit
) {
}
