package cn.openaipay.adapter.walletaccount.web.request;

import jakarta.validation.constraints.NotNull;
/**
 * 创建钱包账户请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateWalletAccountRequest(
        /** 用户ID */
        @NotNull Long userId,
        /** 币种编码 */
        String currencyCode
) {
}
