package cn.openaipay.application.userflow.port;

/**
 * 用户注册查询视图。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record UserRegistrationView(
        /** 用户ID */
        Long userId,
        /** 登录账号 */
        String loginId,
        /** 实名等级 */
        String kycLevel
) {
}
