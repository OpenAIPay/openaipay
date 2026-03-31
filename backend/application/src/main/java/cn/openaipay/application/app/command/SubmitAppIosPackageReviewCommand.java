package cn.openaipay.application.app.command;

/**
 * 提交 iOS 包审核命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record SubmitAppIosPackageReviewCommand(
        /** 版本编码 */
        String versionCode,
        /** submittedBY信息 */
        String submittedBy
) {
}
