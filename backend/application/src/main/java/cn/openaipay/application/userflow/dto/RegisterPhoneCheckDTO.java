package cn.openaipay.application.userflow.dto;

/**
 * 注册手机号校验结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record RegisterPhoneCheckDTO(
        /** 用户是否已存在 */
        boolean userExists,
        /** 是否已完成实名 */
        boolean realNameVerified,
        /** 当前实名等级 */
        String kycLevel
) {
}
