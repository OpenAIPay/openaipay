package cn.openaipay.adapter.bankcard.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 设置默认银行卡请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SetDefaultBankCardRequest(
        /** 用户ID */
        /** 银行卡号 */
                                        @NotNull(message = "userId不能为空") Long userId,
        /** 卡单号 */
        @NotBlank(message = "cardNo不能为空") String cardNo
) {
}
