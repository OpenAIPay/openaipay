package cn.openaipay.adapter.app.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 上报 App 访问记录请求。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record RecordAppVisitRequest(
        /** 设备ID */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String deviceId,
        /** 应用编码 */
        @NotBlank(message = "不能为空") @Size(max = 64, message = "长度不能超过64") String appCode,
        /** 业务ID */
        @Size(max = 64, message = "长度不能超过64") String clientId,
        /** IPaddress信息 */
        @Size(max = 64, message = "长度不能超过64") String ipAddress,
        /** 位置信息 */
        @Size(max = 255, message = "长度不能超过255") String locationInfo,
        /** 业务编码 */
        @Size(max = 64, message = "长度不能超过64") String tenantCode,
        /** 业务类型 */
        @Size(max = 32, message = "长度不能超过32") String networkType,
        /** 当前版本编码 */
        @Size(max = 64, message = "长度不能超过64") String currentVersionCode,
        /** 当前版本号 */
        @Size(max = 64, message = "长度不能超过64") String currentVersionNo,
        /** 设备品牌 */
        @Size(max = 64, message = "长度不能超过64") String deviceBrand,
        /** 系统版本号 */
        @Size(max = 64, message = "长度不能超过64") String osVersion,
        /** API名称 */
        @NotBlank(message = "不能为空") @Size(max = 1024, message = "长度不能超过1024") String apiName,
        /** 请求信息 */
        @Size(max = 5000, message = "长度不能超过5000") String requestParamsText,
        /** 结果汇总信息 */
        @Size(max = 1000, message = "长度不能超过1000") String resultSummary,
        /** durationMS信息 */
        @Min(value = 0, message = "必须大于等于0") Long durationMs
) {
}
