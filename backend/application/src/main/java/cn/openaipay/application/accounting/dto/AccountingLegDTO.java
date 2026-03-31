package cn.openaipay.application.accounting.dto;

import org.joda.money.Money;

/**
 * 会计资金腿DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingLegDTO(
        /** LEG单号 */
        Integer legNo,
        /** 域信息 */
        String accountDomain,
        /** 业务类型 */
        String accountType,
        /** 业务单号 */
        String accountNo,
        /** 所属类型 */
        String ownerType,
        /** 所属ID */
        Long ownerId,
        /** 金额 */
        Money amount,
        /** 方向 */
        String direction,
        /** 业务角色信息 */
        String bizRole,
        /** 科目信息 */
        String subjectHint,
        /** 科目名称 */
        String subjectName,
        /** 业务单号 */
        String referenceNo,
        /** 扩展信息 */
        String metadata
) {
}
