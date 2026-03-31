package cn.openaipay.application.message.command;

/**
 * 发送文本消息命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SendTextMessageCommand(
        /** 发送方用户ID */
        Long senderUserId,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 文本内容 */
        String contentText,
        /** 扩展载荷 */
        String extPayload
) {
}
