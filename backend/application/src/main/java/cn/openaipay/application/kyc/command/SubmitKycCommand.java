package cn.openaipay.application.kyc.command;

/**
 * 提交实名认证命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record SubmitKycCommand(
        /** 用户ID */
        Long userId,
        /** 真实姓名 */
        String realName,
        /** 身份证号 */
        String idCardNo
) {
}
