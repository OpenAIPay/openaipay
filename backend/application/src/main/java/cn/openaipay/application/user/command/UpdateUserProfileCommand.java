package cn.openaipay.application.user.command;
/**
 * 更新用户资料命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdateUserProfileCommand(
        /** 用户ID */
        Long userId,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 手机号 */
        String mobile,
        /** 性别 */
        String gender,
        /** 地区 */
        String region,
        /** 生日 */
        String birthday
) {
}
