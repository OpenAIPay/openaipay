package cn.openaipay.application.kyc.dto;

/**
 * 实名状态数据。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record KycStatusDTO(
        /** 用户ID */
        Long userId,
        /** 实名等级 */
        String kycLevel,
        /** 是否已实名 */
        boolean realNameVerified,
        /** 脱敏实名 */
        String maskedRealName,
        /** 脱敏证件号 */
        String idCardNoMasked
) {
}
