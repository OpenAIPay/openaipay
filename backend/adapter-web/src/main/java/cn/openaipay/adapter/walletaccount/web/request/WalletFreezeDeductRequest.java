package cn.openaipay.adapter.walletaccount.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 钱包冻结扣减请求
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeDeductRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 冻结号 */
        @NotBlank String freezeNo,
        /** 扣减原因 */
        String reason
) {
}
