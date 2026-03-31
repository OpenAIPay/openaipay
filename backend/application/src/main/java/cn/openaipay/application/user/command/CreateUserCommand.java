package cn.openaipay.application.user.command;
/**
 * 创建用户命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record CreateUserCommand(
        /** 爱支付UID */
        String aipayUid,
        /** 登录账号ID */
        String loginId,
        /** 用户类型编码 */
        String userTypeCode,
        /** 账号来源 */
        String accountSource,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 业务编码 */
        String countryCode,
        /** 手机号 */
        String mobile
) {
}
