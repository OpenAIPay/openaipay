package cn.openaipay.application.app.command;

/**
 * 创建应用命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record CreateAppInfoCommand(
        /** 应用编码 */
        String appCode,
        /** 应用名称 */
        String appName
) {
}
