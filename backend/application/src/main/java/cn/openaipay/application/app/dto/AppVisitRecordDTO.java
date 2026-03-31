package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;

/**
 * App 访问记录 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record AppVisitRecordDTO(
        /** 数据库主键ID */
        Long id,
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
        String clientType,
        /** 业务类型 */
        String networkType,
        /** 应用版本ID */
        Long appVersionId,
        /** 设备品牌 */
        String deviceBrand,
        /** 系统版本号 */
        String osVersion,
        /** API名称 */
        String apiName,
        /** 请求信息 */
        String requestParamsText,
        /** 业务时间 */
        LocalDateTime calledAt,
        /** 结果汇总信息 */
        String resultSummary,
        /** durationMS信息 */
        Long durationMs
) {
}
