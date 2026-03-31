package cn.openaipay.adapter.conversation.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 标记会话已读请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record MarkConversationReadRequest(
        /** 用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long userId,
        /** 会话单号 */
        @NotBlank(message = "不能为空") String conversationNo,
        /** 最近已读消息ID */
        String lastReadMessageId
) {
}
