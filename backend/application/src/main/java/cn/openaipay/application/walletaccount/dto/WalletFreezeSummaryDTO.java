package cn.openaipay.application.walletaccount.dto;

import org.joda.money.Money;

import java.util.List;

/**
 * 钱包冻结汇总DTO
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeSummaryDTO(
        /** 用户ID */
        Long userId,
        /** 总冻结金额 */
        Money totalFrozenAmount,
        /** 按类型汇总 */
        List<WalletFreezeSummaryItemDTO> items
) {
}
