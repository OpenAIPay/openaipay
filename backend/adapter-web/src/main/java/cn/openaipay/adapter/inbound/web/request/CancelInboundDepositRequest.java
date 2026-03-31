package cn.openaipay.adapter.inbound.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 入金撤销请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record CancelInboundDepositRequest(
        /** 入金单号 */
        @NotBlank(message = "不能为空") String inboundId,
        /** 业务原因 */
        String reason
) {
}
