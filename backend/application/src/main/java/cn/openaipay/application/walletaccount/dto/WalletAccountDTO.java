package cn.openaipay.application.walletaccount.dto;

import org.joda.money.Money;
/**
 * 钱包账户数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record WalletAccountDTO(
        /** 用户ID */
        Long userId,
        /** 币种编码 */
        String currencyCode,
        /** 可用余额 */
        Money availableBalance,
        /** 冻结余额 */
        Money reservedBalance,
        /** 业务状态 */
        String accountStatus
) {
}
