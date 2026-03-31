package cn.openaipay.adapter.loanaccount.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 借贷账户 TCC Confirm 请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record LoanTccConfirmRequest(
        /** XID */
        @NotBlank String xid,
        /** 分支ID */
        @NotBlank String branchId
) {
}
