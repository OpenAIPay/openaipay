package cn.openaipay.application.user.dto;
/**
 * 用户安全数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UserSecurityDTO(
        /** 用户ID */
        Long userId,
        /** 登录密码SET信息 */
        boolean loginPasswordSet,
        /** 支付密码SET信息 */
        boolean payPasswordSet,
        /** 生物识别开关 */
        boolean biometricEnabled,
        /** TWOfactormode信息 */
        String twoFactorMode,
        /** 风控信息 */
        String riskLevel,
        /** 设备信息 */
        boolean deviceLockEnabled,
        /** 隐私模式开关 */
        boolean privacyModeEnabled
) {
}
