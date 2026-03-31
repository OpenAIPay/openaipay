package cn.openaipay.adapter.walletaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.joda.money.Money;

/**
 * 钱包手工冻结请求
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeHoldRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 冻结号 */
        @NotBlank String freezeNo,
        /** 冻结类型 */
        String freezeType,
        /** 金额 */
        @NotNull @DecimalMin(value = "0.01") Money amount,
        /** 冻结原因 */
        String reason
) {
}
