package cn.openaipay.application.contact.command;

/**
 * 发送好友申请命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record ApplyFriendRequestCommand(
        /** 申请方用户ID */
        Long requesterUserId,
        /** 目标用户ID */
        Long targetUserId,
        /** 业务说明 */
        String applyMessage
) {
}
