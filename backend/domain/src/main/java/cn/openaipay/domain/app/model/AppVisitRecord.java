package cn.openaipay.domain.app.model;

import java.time.LocalDateTime;

/**
 * App 访问记录模型。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public class AppVisitRecord {

    /** 主键ID。 */
    private final Long id;
    /** 设备ID。 */
    private final String deviceId;
    /** 应用编码。 */
    private final String appCode;
    /** 客户端ID。 */
    private final String clientId;
    /** 请求 IP 地址。 */
    private final String ipAddress;
    /** 位置描述。 */
    private final String locationInfo;
    /** 租户编码。 */
    private final String tenantCode;
    /** 客户端类型。 */
    private final AppClientType clientType;
    /** 网络类型。 */
    private final String networkType;
    /** 应用版本ID。 */
    private final Long appVersionId;
    /** 设备品牌。 */
    private final String deviceBrand;
    /** 系统版本号。 */
    private final String osVersion;
    /** 调用 API 名称。 */
    private final String apiName;
    /** 请求参数摘要。 */
    private final String requestParamsText;
    /** 调用时间。 */
    private final LocalDateTime calledAt;
    /** 结果摘要。 */
    private final String resultSummary;
    /** 耗时（毫秒）。 */
    private final Long durationMs;

    public AppVisitRecord(Long id,
                          String deviceId,
                          String appCode,
                          String clientId,
                          String ipAddress,
                          String locationInfo,
                          String tenantCode,
                          AppClientType clientType,
                          String networkType,
                          Long appVersionId,
                          String deviceBrand,
                          String osVersion,
                          String apiName,
                          String requestParamsText,
                          LocalDateTime calledAt,
                          String resultSummary,
                          Long durationMs) {
        this.id = id;
        this.deviceId = AppDomainSupport.normalizeRequired(deviceId, "deviceId");
        this.appCode = AppDomainSupport.normalizeRequired(appCode, "appCode");
        this.clientId = AppDomainSupport.normalizeOptional(clientId);
        this.ipAddress = AppDomainSupport.normalizeOptional(ipAddress);
        this.locationInfo = AppDomainSupport.normalizeOptional(locationInfo);
        this.tenantCode = AppDomainSupport.normalizeOptional(tenantCode);
        this.clientType = clientType == null ? AppClientType.IOS_IPHONE : clientType;
        this.networkType = AppDomainSupport.normalizeOptional(networkType);
        this.appVersionId = appVersionId;
        this.deviceBrand = AppDomainSupport.defaultText(deviceBrand, "APPLE");
        this.osVersion = AppDomainSupport.normalizeOptional(osVersion);
        this.apiName = AppDomainSupport.normalizeRequired(apiName, "apiName");
        this.requestParamsText = AppDomainSupport.normalizeOptional(requestParamsText);
        this.calledAt = AppDomainSupport.defaultNow(calledAt);
        this.resultSummary = AppDomainSupport.normalizeOptional(resultSummary);
        this.durationMs = AppDomainSupport.normalizeNonNegative(durationMs, "durationMs");
    }

    public static AppVisitRecord record(String deviceId,
                                        String appCode,
                                        String clientId,
                                        String ipAddress,
                                        String locationInfo,
                                        String tenantCode,
                                        String networkType,
                                        Long appVersionId,
                                        String deviceBrand,
                                        String osVersion,
                                        String apiName,
                                        String requestParamsText,
                                        String resultSummary,
                                        Long durationMs,
                                        LocalDateTime calledAt) {
        return new AppVisitRecord(
                null,
                deviceId,
                appCode,
                clientId,
                ipAddress,
                locationInfo,
                tenantCode,
                AppClientType.IOS_IPHONE,
                networkType,
                appVersionId,
                deviceBrand,
                osVersion,
                apiName,
                requestParamsText,
                calledAt,
                resultSummary,
                durationMs
        );
    }

    /**
     * 获取ID。
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取设备ID。
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取应用编码。
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * 获取客户端ID。
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 获取IP信息。
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 获取信息。
     */
    public String getLocationInfo() {
        return locationInfo;
    }

    /**
     * 获取编码。
     */
    public String getTenantCode() {
        return tenantCode;
    }

    /**
     * 获取客户端类型信息。
     */
    public AppClientType getClientType() {
        return clientType;
    }

    /**
     * 获取业务数据。
     */
    public String getNetworkType() {
        return networkType;
    }

    /**
     * 获取应用版本ID。
     */
    public Long getAppVersionId() {
        return appVersionId;
    }

    /**
     * 获取设备信息。
     */
    public String getDeviceBrand() {
        return deviceBrand;
    }

    /**
     * 获取OS版本信息。
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * 获取API信息。
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * 获取请求。
     */
    public String getRequestParamsText() {
        return requestParamsText;
    }

    /**
     * 获取AT信息。
     */
    public LocalDateTime getCalledAt() {
        return calledAt;
    }

    /**
     * 获取结果汇总信息。
     */
    public String getResultSummary() {
        return resultSummary;
    }

    /**
     * 获取MS信息。
     */
    public Long getDurationMs() {
        return durationMs;
    }
}
