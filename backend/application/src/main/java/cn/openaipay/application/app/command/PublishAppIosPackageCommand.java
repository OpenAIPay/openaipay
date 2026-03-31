package cn.openaipay.application.app.command;

/**
 * 发布 iOS 包命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record PublishAppIosPackageCommand(
        /** 版本编码 */
        String versionCode
) {
}
