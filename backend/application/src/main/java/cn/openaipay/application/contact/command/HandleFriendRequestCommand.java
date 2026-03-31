package cn.openaipay.application.contact.command;

/**
 * 处理好友申请命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record HandleFriendRequestCommand(
        /** 用户ID */
        Long operatorUserId,
        /** 请求幂等号 */
        String requestNo,
        /** 处理动作 */
        String action
) {
}
