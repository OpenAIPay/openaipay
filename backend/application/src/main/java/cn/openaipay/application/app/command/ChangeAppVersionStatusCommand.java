package cn.openaipay.application.app.command;

/**
 * 变更应用版本状态命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record ChangeAppVersionStatusCommand(
        /** 版本编码 */
        String versionCode,
        /** 状态编码 */
        String status
) {
}
