package cn.openaipay.application.user.command;
/**
 * 更新用户安全命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdateUserSecurityCommand(
        /** 用户ID */
        Long userId,
        /** 登录密码SET信息 */
        Boolean loginPasswordSet,
        /** 支付密码SET信息 */
        Boolean payPasswordSet,
        /** 生物识别开关 */
        Boolean biometricEnabled,
        /** TWOfactormode信息 */
        String twoFactorMode,
        /** 风控信息 */
        String riskLevel,
        /** 设备信息 */
        Boolean deviceLockEnabled,
        /** 隐私模式开关 */
        Boolean privacyModeEnabled
) {
}
