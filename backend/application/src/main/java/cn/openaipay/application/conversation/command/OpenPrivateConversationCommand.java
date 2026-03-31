package cn.openaipay.application.conversation.command;

/**
 * 打开私聊会话命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record OpenPrivateConversationCommand(
        /** 用户ID */
        Long userId,
        /** 对端用户ID */
        Long peerUserId
) {
}
