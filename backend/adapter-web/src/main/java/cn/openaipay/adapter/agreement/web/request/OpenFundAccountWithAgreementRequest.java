package cn.openaipay.adapter.agreement.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 签约并开通基金账户请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record OpenFundAccountWithAgreementRequest(
        /** 用户ID */
        @NotNull(message = "must not be null")
                                        @Positive(message = "must be greater than 0")
                                        Long userId,
        /** 资金编码 */
        String fundCode,
        /** 币种编码 */
        String currencyCode,
        /** 业务键 */
        @NotBlank(message = "must not be blank")
                                        String idempotencyKey,
        /** 协议信息 */
        @NotEmpty(message = "must not be empty")
                                        List<@Valid AgreementAcceptRequest> agreementAccepts
) {
}
