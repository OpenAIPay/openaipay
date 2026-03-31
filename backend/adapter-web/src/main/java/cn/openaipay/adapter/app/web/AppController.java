package cn.openaipay.adapter.app.web;

import cn.openaipay.adapter.app.web.request.RecordAppBehaviorEventRequest;
import cn.openaipay.adapter.app.web.request.RecordAppVisitRequest;
import cn.openaipay.adapter.app.web.request.UpsertAppDeviceRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.auth.exception.UnauthorizedException;
import cn.openaipay.application.app.command.RecordAppBehaviorEventCommand;
import cn.openaipay.application.app.command.CheckAppVersionCommand;
import cn.openaipay.application.app.command.RecordAppVisitCommand;
import cn.openaipay.application.app.command.UpsertAppDeviceCommand;
import cn.openaipay.application.app.dto.AppBehaviorEventDTO;
import cn.openaipay.application.app.dto.AppDemoProvisioningConfigDTO;
import cn.openaipay.application.app.dto.AppDeviceDTO;
import cn.openaipay.application.app.dto.AppVersionCheckDTO;
import cn.openaipay.application.app.dto.AppVisitRecordDTO;
import cn.openaipay.application.app.facade.AppFacade;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 客户端版本与设备控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@RestController
@RequestMapping("/api/apps")
public class AppController {

    /** App 应用服务。 */
    private final AppFacade appFacade;
    /** 运行时环境配置。 */
    private final Environment environment;

    /** AppController 业务接口。 */
    public AppController(AppFacade appFacade, Environment environment) {
        this.appFacade = appFacade;
        this.environment = environment;
    }

    /** 检查指定应用在当前设备上的版本更新信息。 */
    /**
     * 校验版本信息。
     */
    @GetMapping("/{appCode}/versions/check")
    public ApiResponse<AppVersionCheckDTO> checkVersion(@PathVariable("appCode") String appCode,
                                                        @RequestParam(value = "currentVersionNo", required = false) String currentVersionNo,
                                                        @RequestParam(value = "deviceId", required = false) String deviceId) {
        return ApiResponse.success(appFacade.checkVersion(new CheckAppVersionCommand(appCode, currentVersionNo, deviceId)));
    }

    /**
     * 查询演示账号初始化配置（仅供 BFF 内部调用）。
     */
    @GetMapping("/{appCode}/demo-provisioning-config")
    public ApiResponse<AppDemoProvisioningConfigDTO> getDemoProvisioningConfig(
            @PathVariable("appCode") String appCode,
            @RequestHeader(value = "X-Internal-Token", required = false) String internalToken
    ) {
        String expectedToken = normalizeOptional(environment.getProperty("OPENAIPAY_INTERNAL_CONFIG_TOKEN"));
        if (expectedToken == null || !expectedToken.equals(normalizeOptional(internalToken))) {
            throw new UnauthorizedException("内部配置访问凭证无效");
        }
        return ApiResponse.success(appFacade.getDemoProvisioningConfig(appCode));
    }

    /** 新增或更新设备安装与活跃信息。 */
    /**
     * 保存或更新设备信息。
     */
    @PostMapping("/devices")
    public ApiResponse<AppDeviceDTO> upsertDevice(@Valid @RequestBody UpsertAppDeviceRequest request) {
        return ApiResponse.success(appFacade.upsertDevice(new UpsertAppDeviceCommand(
                request.deviceId(),
                request.appCode(),
                request.clientIds(),
                request.deviceBrand(),
                request.osVersion(),
                request.currentVersionCode(),
                request.currentVersionNo(),
                request.started()
        )));
    }

    /** 记录客户端访问与接口调用轨迹。 */
    /**
     * 记录访问信息。
     */
    @PostMapping("/visit-records")
    public ApiResponse<AppVisitRecordDTO> recordVisit(@Valid @RequestBody RecordAppVisitRequest request) {
        return ApiResponse.success(appFacade.recordVisit(new RecordAppVisitCommand(
                request.deviceId(),
                request.appCode(),
                request.clientId(),
                request.ipAddress(),
                request.locationInfo(),
                request.tenantCode(),
                request.networkType(),
                request.currentVersionCode(),
                request.currentVersionNo(),
                request.deviceBrand(),
                request.osVersion(),
                request.apiName(),
                request.requestParamsText(),
                request.resultSummary(),
                request.durationMs()
        )));
    }

    /** 记录客户端用户行为埋点。 */
    @PostMapping("/behavior-events")
    public ApiResponse<AppBehaviorEventDTO> recordBehaviorEvent(@Valid @RequestBody RecordAppBehaviorEventRequest request) {
        return ApiResponse.success(appFacade.recordBehaviorEvent(new RecordAppBehaviorEventCommand(
                request.eventId(),
                request.sessionId(),
                request.appCode(),
                request.eventName(),
                request.eventType(),
                request.eventCode(),
                request.pageName(),
                request.actionName(),
                request.resultStatus(),
                request.traceId(),
                request.deviceId(),
                request.clientId(),
                request.userId(),
                request.aipayUid(),
                request.loginId(),
                request.accountStatus(),
                request.kycLevel(),
                request.nickname(),
                request.mobile(),
                request.ipAddress(),
                request.locationInfo(),
                request.tenantCode(),
                request.networkType(),
                request.appVersionNo(),
                request.appBuildNo(),
                request.deviceBrand(),
                request.deviceModel(),
                request.deviceName(),
                request.deviceType(),
                request.osName(),
                request.osVersion(),
                request.locale(),
                request.timezone(),
                request.language(),
                request.countryCode(),
                request.carrierName(),
                request.screenWidth(),
                request.screenHeight(),
                request.viewportWidth(),
                request.viewportHeight(),
                request.durationMs(),
                request.loginDurationMs(),
                request.eventAtEpochMs(),
                request.payloadJson()
        )));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
