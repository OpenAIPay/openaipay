package cn.openaipay.adapter.inbound.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.joda.money.Money;

/**
 * 入金提交请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SubmitInboundDepositRequest(
        /** 请求业务单号 */
        @NotBlank(message = "不能为空") String requestBizNo,
        /** 支付单号 */
        @NotBlank(message = "不能为空") String payOrderNo,
        /** 付款方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long payerUserId,
        /** 付款方账号 */
        @NotBlank(message = "不能为空") String payerAccountNo,
        /** 金额 */
        @NotNull(message = "不能为空") Money amount,
        /** 支付渠道编码 */
        String payChannelCode,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 请求幂等标识 */
        String requestIdentify,
        /** 业务身份标识 */
        String bizIdentity
) {
}
