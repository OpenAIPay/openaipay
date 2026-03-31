package cn.openaipay.application.app.command;

/**
 * 客户端版本检查命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record CheckAppVersionCommand(
        /** 应用编码 */
        String appCode,
        /** 当前版本号 */
        String currentVersionNo,
        /** 设备ID */
        String deviceId
) {
}
