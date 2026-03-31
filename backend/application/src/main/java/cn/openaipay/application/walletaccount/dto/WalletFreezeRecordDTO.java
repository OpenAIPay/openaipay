package cn.openaipay.application.walletaccount.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;

/**
 * 钱包冻结明细DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeRecordDTO(
        /** 用户ID */
        Long userId,
        /** 冻结号 */
        String freezeNo,
        /** 冻结类型 */
        String freezeType,
        /** 操作类型 */
        String operationType,
        /** 冻结状态 */
        String freezeStatus,
        /** 金额 */
        Money amount,
        /** 冻结原因 */
        String freezeReason,
        /** 创建时间 */
        LocalDateTime createdAt,
        /** 更新时间 */
        LocalDateTime updatedAt
) {
}
