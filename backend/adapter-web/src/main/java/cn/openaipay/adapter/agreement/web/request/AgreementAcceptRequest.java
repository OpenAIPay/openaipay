package cn.openaipay.adapter.agreement.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 协议同意项请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record AgreementAcceptRequest(
        /** 模板编码 */
        @NotBlank(message = "must not be blank")
                                        String templateCode,
        /** 模板版本号 */
        @NotBlank(message = "must not be blank")
                                        String templateVersion
) {
}
