package cn.openaipay.application.accounting.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 会计分录DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingEntryDTO(
        /** 业务单号 */
        String voucherNo,
        /** 业务单号 */
        Integer lineNo,
        /** 科目编码 */
        String subjectCode,
        /** 科目名称 */
        String subjectName,
        /** DC标记 */
        String dcFlag,
        /** 金额 */
        Money amount,
        /** 所属类型 */
        String ownerType,
        /** 所属ID */
        Long ownerId,
        /** 域信息 */
        String accountDomain,
        /** 业务类型 */
        String accountType,
        /** 业务单号 */
        String accountNo,
        /** 业务角色信息 */
        String bizRole,
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
        /** 业务单号 */
        String referenceNo,
        /** 分录备注 */
        String entryMemo,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
