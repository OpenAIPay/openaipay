package cn.openaipay.adapter.walletaccount.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 钱包手工解冻请求
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeReleaseRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 冻结号 */
        @NotBlank String freezeNo,
        /** 解冻原因 */
        String reason
) {
}
