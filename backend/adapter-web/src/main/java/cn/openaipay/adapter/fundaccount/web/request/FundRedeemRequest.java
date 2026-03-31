package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金赎回请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundRedeemRequest(
        /** 订单号 */
        @NotBlank String orderNo,
        /** 用户ID */
        @NotNull Long userId,
        /** 资金编码 */
        String fundCode,
        /** 份额 */
        @NotNull @DecimalMin(value = "0.0001") FundAmount share,
        /** 赎回模式 */
        String redeemMode,
        /** 业务单号 */
        String businessNo,
        /** 转出去向：BALANCE/BANK_CARD */
        String redeemDestination,
        /** 转出银行卡名称 */
        String bankName
) {
}
