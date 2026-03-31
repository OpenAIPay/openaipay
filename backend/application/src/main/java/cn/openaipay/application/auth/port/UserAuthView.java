package cn.openaipay.application.auth.port;
/**
 * 用户认证View记录
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UserAuthView(
        /** 用户ID */
        Long userId,
        /** 爱支付UID */
        String aipayUid,
        /** 登录账号ID */
        String loginId,
        /** 业务状态 */
        String accountStatus,
        /** KYClevel信息 */
        String kycLevel,
        /** 登录密码SHA256信息 */
        String loginPasswordSha256,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 手机号 */
        String mobile,
        /** 业务名称 */
        String maskedRealName,
        /** ID卡单号 */
        String idCardNoMasked,
        /** 业务编码 */
        String countryCode,
        /** 性别 */
        String gender,
        /** 地区 */
        String region
) {
}
