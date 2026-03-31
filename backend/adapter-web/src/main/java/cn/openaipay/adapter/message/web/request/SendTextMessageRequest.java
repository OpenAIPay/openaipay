package cn.openaipay.adapter.message.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送文本消息请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SendTextMessageRequest(
        /** 发送方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long senderUserId,
        /** 接收方用户ID */
        @NotNull(message = "不能为空") @Min(value = 1, message = "必须大于0") Long receiverUserId,
        /** 文本内容 */
        @NotBlank(message = "不能为空") String contentText,
        /** 扩展载荷 */
        String extPayload
) {
}
