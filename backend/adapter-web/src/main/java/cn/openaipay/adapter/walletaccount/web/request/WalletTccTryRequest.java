package cn.openaipay.adapter.walletaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.joda.money.Money;
/**
 * 钱包TCCTry请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record WalletTccTryRequest(
        /** XID */
        @NotBlank String xid,
        /** 分支ID */
        @NotBlank String branchId,
        /** 用户ID */
        @NotNull Long userId,
        /** 业务类型 */
        @NotBlank String operationType,
        /** 冻结类型 */
        String freezeType,
        /** 金额 */
        @NotNull @DecimalMin(value = "0.01") Money amount,
        /** 业务单号 */
        String businessNo
) {
}
