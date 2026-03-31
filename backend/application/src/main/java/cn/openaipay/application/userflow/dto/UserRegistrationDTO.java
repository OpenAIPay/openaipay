package cn.openaipay.application.userflow.dto;

/**
 * 注册结果数据。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record UserRegistrationDTO(
        /** 用户ID */
        Long userId,
        /** 爱付UID */
        String aipayUid,
        /** 登录账号 */
        String loginId,
        /** 是否已提交实名 */
        boolean kycSubmitted
) {
}
