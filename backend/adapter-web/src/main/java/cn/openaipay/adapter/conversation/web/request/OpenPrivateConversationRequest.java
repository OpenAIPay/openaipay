package cn.openaipay.adapter.conversation.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 打开私聊会话请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record OpenPrivateConversationRequest(
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId,
        /** 对端用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long peerUserId
) {
}
