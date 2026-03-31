package cn.openaipay.adapter.gateway.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 网关提现查单请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record GatewayWithdrawQueryRequest(
        /** 出金单号 */
        @NotBlank(message = "不能为空") String outboundId,
        /** 机构渠道编码 */
        @NotBlank(message = "不能为空") String instChannelCode
) {
}
