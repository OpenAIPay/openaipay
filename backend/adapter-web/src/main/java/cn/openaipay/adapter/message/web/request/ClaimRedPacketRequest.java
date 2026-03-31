package cn.openaipay.adapter.message.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 领取红包请求参数。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record ClaimRedPacketRequest(
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId
) {
}
