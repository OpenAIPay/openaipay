package cn.openaipay.application.accounting.dto;

import org.joda.money.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会计凭证DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingVoucherDTO(
        /** 业务单号 */
        String voucherNo,
        /** 业务ID */
        String bookId,
        /** 业务类型 */
        String voucherType,
        /** 事件ID */
        String eventId,
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
        /** 业务场景编码 */
        String businessSceneCode,
        /** 业务域编码 */
        String businessDomainCode,
        /** 状态编码 */
        String status,
        /** 币种编码 */
        String currencyCode,
        /** 总金额 */
        Money totalDebitAmount,
        /** 总信用金额 */
        Money totalCreditAmount,
        /** 业务时间 */
        LocalDateTime occurredAt,
        /** 业务日期 */
        LocalDate postingDate,
        /** 业务单号 */
        String reversedVoucherNo,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt,
        /** 分录列表 */
        List<AccountingEntryDTO> entries
) {
}
