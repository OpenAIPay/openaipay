package cn.openaipay.application.message.command;

/**
 * 发送图片消息命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SendImageMessageCommand(
        /** 发送方用户ID */
        Long senderUserId,
        /** 接收方用户ID */
        Long receiverUserId,
        /** 媒体资源ID */
        String mediaId,
        /** 扩展载荷 */
        String extPayload
) {
}
