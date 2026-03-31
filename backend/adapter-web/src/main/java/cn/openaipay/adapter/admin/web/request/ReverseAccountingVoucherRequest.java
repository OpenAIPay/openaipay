package cn.openaipay.adapter.admin.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 冲正会计凭证请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record ReverseAccountingVoucherRequest(
        /** 业务原因 */
        @NotBlank(message = "不能为空") String reverseReason
) {
}
