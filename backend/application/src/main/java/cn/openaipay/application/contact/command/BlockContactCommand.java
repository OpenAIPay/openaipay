package cn.openaipay.application.contact.command;

/**
 * 拉黑联系人命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record BlockContactCommand(
        /** 所属用户ID */
        Long ownerUserId,
        /** 用户ID */
        Long blockedUserId,
        /** 业务原因 */
        String reason
) {
}
