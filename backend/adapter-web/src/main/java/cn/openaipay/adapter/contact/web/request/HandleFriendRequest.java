package cn.openaipay.adapter.contact.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 处理好友申请请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record HandleFriendRequest(
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long operatorUserId,
        /** 处理动作 */
        @NotBlank(message = "不能为空") String action
) {
}
