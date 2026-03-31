package cn.openaipay.application.walletaccount.command;

import org.joda.money.Money;

/**
 * 钱包冻结命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeHoldCommand(
        /** 用户ID */
        Long userId,
        /** 冻结号 */
        String freezeNo,
        /** 冻结类型 */
        String freezeType,
        /** 金额 */
        Money amount,
        /** 冻结原因 */
        String reason
) {
}
