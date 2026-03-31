package cn.openaipay.application.conversation.command;

/**
 * 标记会话已读命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record MarkConversationReadCommand(
        /** 用户ID */
        Long userId,
        /** 会话单号 */
        String conversationNo,
        /** 最近已读消息ID */
        String lastReadMessageId
) {
}
