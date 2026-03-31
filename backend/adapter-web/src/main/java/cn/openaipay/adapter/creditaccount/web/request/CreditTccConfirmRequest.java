package cn.openaipay.adapter.creditaccount.web.request;

import jakarta.validation.constraints.NotBlank;
/**
 * 信用TCCConfirm请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreditTccConfirmRequest(
        /** XID */
        @NotBlank String xid,
        /** 分支ID */
        @NotBlank String branchId
) {
}
