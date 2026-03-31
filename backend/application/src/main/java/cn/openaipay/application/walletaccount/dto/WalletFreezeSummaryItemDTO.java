package cn.openaipay.application.walletaccount.dto;

import org.joda.money.Money;

/**
 * 钱包冻结汇总项DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeSummaryItemDTO(
        /** 冻结类型 */
        String freezeType,
        /** 冻结金额 */
        Money amount
) {
}
