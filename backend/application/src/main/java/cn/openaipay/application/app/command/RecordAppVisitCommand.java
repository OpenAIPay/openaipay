package cn.openaipay.application.app.command;

/**
 * 记录 App 访问事件命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record RecordAppVisitCommand(
        /** 设备ID */
        String deviceId,
        /** 应用编码 */
        String appCode,
        /** 业务ID */
        String clientId,
        /** IPaddress信息 */
        String ipAddress,
        /** 位置信息 */
        String locationInfo,
        /** 业务编码 */
        String tenantCode,
        /** 业务类型 */
        String networkType,
        /** 当前版本编码 */
        String currentVersionCode,
        /** 当前版本号 */
        String currentVersionNo,
        /** 设备品牌 */
        String deviceBrand,
        /** 系统版本号 */
        String osVersion,
        /** API名称 */
        String apiName,
        /** 请求信息 */
        String requestParamsText,
        /** 结果汇总信息 */
        String resultSummary,
        /** durationMS信息 */
        Long durationMs
) {
}
