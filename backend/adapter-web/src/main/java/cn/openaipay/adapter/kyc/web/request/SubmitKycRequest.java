package cn.openaipay.adapter.kyc.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 提交实名认证请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record SubmitKycRequest(
        /** 真实姓名 */
        @NotBlank String realName,
        /** 身份证号 */
        @NotBlank String idCardNo
) {
}
