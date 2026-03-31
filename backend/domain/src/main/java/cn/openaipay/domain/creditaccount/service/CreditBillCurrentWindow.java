package cn.openaipay.domain.creditaccount.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 当前账单窗口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record CreditBillCurrentWindow(
        /** 业务日期 */
        LocalDate statementDate,
        /** 账期开始时间（含） */
        LocalDateTime cycleStartInclusive,
        /** 账期结束时间（不含） */
        LocalDateTime cycleEndExclusive,
        /** 未出账开始时间（含） */
        LocalDateTime unbilledStartInclusive,
        /** unbilledENDexclusive信息 */
        LocalDateTime unbilledEndExclusive
) {
}
