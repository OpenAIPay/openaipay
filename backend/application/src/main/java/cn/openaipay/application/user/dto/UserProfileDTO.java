package cn.openaipay.application.user.dto;
/**
 * 用户资料数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UserProfileDTO(
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
        /** 账号来源 */
        String accountSource,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 业务编码 */
        String countryCode,
        /** 手机号 */
        String mobile,
        /** 业务名称 */
        String maskedRealName,
        /** ID卡单号 */
        String idCardNo,
        /** 性别 */
        String gender,
        /** 地区 */
        String region,
        /** 生日 */
        String birthday
) {
}
