package cn.openaipay.adapter.walletaccount.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 钱包TCCConfirm请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record WalletTccConfirmRequest(
        /** XID */
        @NotBlank String xid,
        /** 分支ID */
        @NotBlank String branchId
) {
}
