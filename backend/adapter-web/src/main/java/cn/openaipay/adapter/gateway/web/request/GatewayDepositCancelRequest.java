package cn.openaipay.adapter.gateway.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 网关入金撤销请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayDepositCancelRequest(
        /** 入金单号 */
        @NotBlank(message = "不能为空") String inboundId,
        /** 机构渠道编码 */
        @NotBlank(message = "不能为空") String instChannelCode,
        /** 业务原因 */
        String reason
) {
}
