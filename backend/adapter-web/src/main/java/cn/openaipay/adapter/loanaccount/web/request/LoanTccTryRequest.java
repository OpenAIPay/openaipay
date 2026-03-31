package cn.openaipay.adapter.loanaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.joda.money.Money;

/**
 * 借贷账户 TCC Try 请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record LoanTccTryRequest(
        /** XID */
        @NotBlank String xid,
        /** 分支ID */
        @NotBlank String branchId,
        /** 业务单号 */
        @NotBlank String accountNo,
        /** 业务类型 */
        @NotBlank String operationType,
        /** 资源信息 */
        @NotBlank String assetCategory,
        /** 金额 */
        @NotNull @DecimalMin(value = "0.01") Money amount,
        /** 业务单号 */
        String businessNo
) {
}
