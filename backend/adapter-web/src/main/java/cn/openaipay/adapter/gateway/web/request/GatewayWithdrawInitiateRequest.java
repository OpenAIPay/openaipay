package cn.openaipay.adapter.gateway.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.joda.money.Money;

/**
 * 网关提现受理请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayWithdrawInitiateRequest(
        /** 出金单号 */
        @NotBlank(message = "不能为空") String outboundId,
        /** 机构渠道编码 */
        @NotBlank(message = "不能为空") String instChannelCode,
        /** 付款方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long payerUserId,
        /** 收款方账号 */
        @NotBlank(message = "不能为空") String payeeAccountNo,
        /** 金额 */
        @NotNull(message = "不能为空") @DecimalMin(value = "0.01", message = "必须大于0") Money amount,
        /** 支付渠道编码 */
        String payChannelCode,
        /** 请求幂等标识 */
        String requestIdentify,
        /** 业务身份标识 */
        String bizIdentity
) {
}
