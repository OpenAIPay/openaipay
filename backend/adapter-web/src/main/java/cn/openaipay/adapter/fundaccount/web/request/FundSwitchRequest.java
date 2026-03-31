package cn.openaipay.adapter.fundaccount.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import cn.openaipay.domain.shared.number.FundAmount;
/**
 * 基金切换请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundSwitchRequest(
        /** 订单号 */
        @NotBlank String orderNo,
        /** 用户ID */
        @NotNull Long userId,
        /** 来源资金编码 */
        @NotBlank String sourceFundCode,
        /** 目标资金编码 */
        @NotBlank String targetFundCode,
        /** 来源信息 */
        @NotNull @DecimalMin(value = "0.0001") FundAmount sourceShare,
        /** 业务单号 */
        String businessNo
) {
}
