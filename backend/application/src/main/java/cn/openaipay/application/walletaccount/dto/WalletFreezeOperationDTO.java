package cn.openaipay.application.walletaccount.dto;

import org.joda.money.Money;

/**
 * 钱包冻结操作结果
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeOperationDTO(
        /** 用户ID */
        Long userId,
        /** 冻结号 */
        String freezeNo,
        /** 冻结类型 */
        String freezeType,
        /** 冻结状态 */
        String freezeStatus,
        /** 金额 */
        Money amount,
        /** 可用余额 */
        Money availableBalance,
        /** 冻结余额 */
        Money reservedBalance,
        /** 结果说明 */
        String message
) {
}
