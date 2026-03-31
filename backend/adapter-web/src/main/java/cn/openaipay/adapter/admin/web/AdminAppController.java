package cn.openaipay.adapter.admin.web;

import cn.openaipay.adapter.admin.security.AdminRequestContext;
import cn.openaipay.adapter.admin.security.RequireAdminPermission;
import cn.openaipay.adapter.admin.web.request.ChangeAppVersionStatusRequest;
import cn.openaipay.adapter.admin.web.request.CreateAppRequest;
import cn.openaipay.adapter.admin.web.request.CreateAppVersionRequest;
import cn.openaipay.adapter.admin.web.request.UpdateAppVersionRequest;
import cn.openaipay.adapter.admin.web.request.UpdateAppSettingsRequest;
import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.application.app.command.ChangeAppVersionStatusCommand;
import cn.openaipay.application.app.command.CreateAppInfoCommand;
import cn.openaipay.application.app.command.CreateAppVersionCommand;
import cn.openaipay.application.app.command.GenerateAppBehaviorEventReportCommand;
import cn.openaipay.application.app.command.PublishAppIosPackageCommand;
import cn.openaipay.application.app.command.QueryAppBehaviorEventsCommand;
import cn.openaipay.application.app.command.SummarizeAppBehaviorEventsCommand;
import cn.openaipay.application.app.command.SubmitAppIosPackageReviewCommand;
import cn.openaipay.application.app.command.UpdateAppVersionCommand;
import cn.openaipay.application.app.command.UpdateAppSettingsCommand;
import cn.openaipay.application.app.dto.AppBehaviorEventDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventReportDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventReportRowDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventStatsDTO;
import cn.openaipay.application.app.dto.AppDeviceDTO;
import cn.openaipay.application.app.dto.AppInfoDTO;
import cn.openaipay.application.app.dto.AppVersionDTO;
import cn.openaipay.application.app.dto.AppVisitRecordDTO;
import cn.openaipay.application.app.facade.AppFacade;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 App 版本管理控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@RestController
@RequestMapping("/api/admin/apps")
public class AdminAppController {

    /** 支持的时间格式（秒级）。 */
    private static final DateTimeFormatter SECOND_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 支持的时间格式（分钟级）。 */
    private static final DateTimeFormatter MINUTE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    /** 支持的时间格式（ISO，分钟级）。 */
    private static final DateTimeFormatter ISO_MINUTE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /** 应用管理门面。 */
    private final AppFacade appFacade;
    /** 管理后台请求上下文。 */
    private final AdminRequestContext adminRequestContext;

    public AdminAppController(AppFacade appFacade,
                              AdminRequestContext adminRequestContext) {
        this.appFacade = appFacade;
        this.adminRequestContext = adminRequestContext;
    }

    /**
     * 查询应用信息列表。
     */
    @GetMapping
    @RequireAdminPermission("app.info.list")
    public ApiResponse<List<AppInfoDTO>> listApps() {
        return ApiResponse.success(appFacade.listApps());
    }

    /**
     * 更新应用设置信息。
     */
    @PutMapping("/{appCode}/settings")
    @RequireAdminPermission("app.settings.update")
    public ApiResponse<AppInfoDTO> updateAppSettings(@PathVariable("appCode") String appCode,
                                                     @Valid @RequestBody UpdateAppSettingsRequest request) {
        return ApiResponse.success(appFacade.updateAppSettings(new UpdateAppSettingsCommand(
                appCode,
                request.versionPromptEnabled(),
                request.demoAutoLoginEnabled(),
                request.loginDeviceBindingCheckEnabled(),
                request.demoTemplateLoginId(),
                request.demoContactLoginId(),
                request.demoLoginPassword()
        )));
    }

    /**
     * 创建应用信息。
     */
    @PostMapping
    @RequireAdminPermission("app.info.create")
    public ApiResponse<AppInfoDTO> createApp(@Valid @RequestBody CreateAppRequest request) {
        return ApiResponse.success(appFacade.createApp(new CreateAppInfoCommand(request.appCode(), request.appName())));
    }

    /**
     * 查询版本信息列表。
     */
    @GetMapping("/{appCode}/versions")
    @RequireAdminPermission("app.version.list")
    public ApiResponse<List<AppVersionDTO>> listVersions(@PathVariable("appCode") String appCode) {
        return ApiResponse.success(appFacade.listVersions(appCode));
    }

    /**
     * 创建版本信息。
     */
    @PostMapping("/{appCode}/versions")
    @RequireAdminPermission("app.version.create")
    public ApiResponse<AppVersionDTO> createVersion(@PathVariable("appCode") String appCode,
                                                    @Valid @RequestBody CreateAppVersionRequest request) {
        return ApiResponse.success(appFacade.createVersion(new CreateAppVersionCommand(
                appCode,
                request.versionCode(),
                request.appVersionNo(),
                request.updateType(),
                request.updatePromptFrequency(),
                request.versionDescription(),
                request.publisherRemark(),
                request.releaseRegions(),
                request.targetedRegions(),
                request.minSupportedVersionNo(),
                request.iosCode(),
                request.appStoreUrl(),
                request.packageSizeBytes(),
                request.md5()
        )));
    }

    /**
     * 更新版本信息。
     */
    @PutMapping("/versions/{versionCode}")
    @RequireAdminPermission("app.version.create")
    public ApiResponse<AppVersionDTO> updateVersion(@PathVariable("versionCode") String versionCode,
                                                    @Valid @RequestBody UpdateAppVersionRequest request) {
        return ApiResponse.success(appFacade.updateVersion(new UpdateAppVersionCommand(
                versionCode,
                request.updateType(),
                request.updatePromptFrequency(),
                request.versionDescription(),
                request.publisherRemark(),
                request.minSupportedVersionNo(),
                request.iosCode(),
                request.appStoreUrl(),
                request.packageSizeBytes(),
                request.md5()
        )));
    }

    /**
     * 处理版本状态。
     */
    @PutMapping("/versions/{versionCode}/status")
    @RequireAdminPermission("app.version.update-status")
    public ApiResponse<AppVersionDTO> changeVersionStatus(@PathVariable("versionCode") String versionCode,
                                                          @Valid @RequestBody ChangeAppVersionStatusRequest request) {
        return ApiResponse.success(appFacade.changeVersionStatus(new ChangeAppVersionStatusCommand(versionCode, request.status())));
    }

    /**
     * 提交IOS信息。
     */
    @PutMapping("/versions/{versionCode}/ios-package/submit-review")
    @RequireAdminPermission("app.ios-package.submit-review")
    public ApiResponse<AppVersionDTO> submitIosPackageReview(@PathVariable("versionCode") String versionCode) {
        return ApiResponse.success(appFacade.submitIosPackageReview(new SubmitAppIosPackageReviewCommand(versionCode, resolveOperator())));
    }

    /**
     * 发布IOS信息。
     */
    @PutMapping("/versions/{versionCode}/ios-package/publish")
    @RequireAdminPermission("app.ios-package.publish")
    public ApiResponse<AppVersionDTO> publishIosPackage(@PathVariable("versionCode") String versionCode) {
        return ApiResponse.success(appFacade.publishIosPackage(new PublishAppIosPackageCommand(versionCode)));
    }

    /**
     * 查询设备信息列表。
     */
    @GetMapping("/{appCode}/devices")
    @RequireAdminPermission("app.device.list")
    public ApiResponse<List<AppDeviceDTO>> listDevices(@PathVariable("appCode") String appCode) {
        return ApiResponse.success(appFacade.listDevices(appCode));
    }

    /**
     * 查询访问记录列表。
     */
    @GetMapping("/devices/{deviceId}/visit-records")
    @RequireAdminPermission("app.visit.list")
    public ApiResponse<List<AppVisitRecordDTO>> listVisitRecords(@PathVariable("deviceId") String deviceId,
                                                                 @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(appFacade.listRecentVisitRecords(deviceId, limit));
    }

    /**
     * 查询行为埋点明细列表。
     */
    @GetMapping("/{appCode}/behavior-events")
    @RequireAdminPermission("app.visit.list")
    public ApiResponse<List<AppBehaviorEventDTO>> listBehaviorEvents(
            @PathVariable("appCode") String appCode,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "eventName", required = false) String eventName,
            @RequestParam(value = "pageName", required = false) String pageName,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startAt", required = false) String startAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(appFacade.listBehaviorEvents(new QueryAppBehaviorEventsCommand(
                appCode,
                eventType,
                eventName,
                pageName,
                deviceId,
                userId,
                parseDateTime(startAt, "startAt"),
                parseDateTime(endAt, "endAt"),
                limit
        )));
    }

    /**
     * 聚合行为埋点统计数据。
     */
    @GetMapping("/{appCode}/behavior-events/stats")
    @RequireAdminPermission("app.visit.list")
    public ApiResponse<AppBehaviorEventStatsDTO> summarizeBehaviorEvents(
            @PathVariable("appCode") String appCode,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "eventName", required = false) String eventName,
            @RequestParam(value = "pageName", required = false) String pageName,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startAt", required = false) String startAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            @RequestParam(value = "topLimit", required = false) Integer topLimit) {
        return ApiResponse.success(appFacade.summarizeBehaviorEvents(new SummarizeAppBehaviorEventsCommand(
                appCode,
                eventType,
                eventName,
                pageName,
                deviceId,
                userId,
                parseDateTime(startAt, "startAt"),
                parseDateTime(endAt, "endAt"),
                topLimit
        )));
    }

    /**
     * 生成行为埋点报表。
     */
    @GetMapping("/{appCode}/behavior-events/report")
    @RequireAdminPermission("app.visit.list")
    public ApiResponse<AppBehaviorEventReportDTO> generateBehaviorEventReport(
            @PathVariable("appCode") String appCode,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "eventName", required = false) String eventName,
            @RequestParam(value = "pageName", required = false) String pageName,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startAt", required = false) String startAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ApiResponse.success(appFacade.generateBehaviorEventReport(new GenerateAppBehaviorEventReportCommand(
                appCode,
                eventType,
                eventName,
                pageName,
                deviceId,
                userId,
                parseDateTime(startAt, "startAt"),
                parseDateTime(endAt, "endAt"),
                limit
        )));
    }

    /**
     * 导出行为埋点报表 CSV。
     */
    @GetMapping(value = "/{appCode}/behavior-events/report.csv", produces = "text/csv;charset=UTF-8")
    @RequireAdminPermission("app.visit.list")
    public ResponseEntity<String> exportBehaviorEventReportCsv(
            @PathVariable("appCode") String appCode,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "eventName", required = false) String eventName,
            @RequestParam(value = "pageName", required = false) String pageName,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "startAt", required = false) String startAt,
            @RequestParam(value = "endAt", required = false) String endAt,
            @RequestParam(value = "limit", required = false) Integer limit) {
        AppBehaviorEventReportDTO report = appFacade.generateBehaviorEventReport(new GenerateAppBehaviorEventReportCommand(
                appCode,
                eventType,
                eventName,
                pageName,
                deviceId,
                userId,
                parseDateTime(startAt, "startAt"),
                parseDateTime(endAt, "endAt"),
                limit
        ));
        String fileName = "app_behavior_report_" + sanitizeFileToken(appCode) + "_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(toBehaviorReportCsv(report));
    }

    private String toBehaviorReportCsv(AppBehaviorEventReportDTO report) {
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append("统计日期,事件类型,事件名称,事件总数,成功数,失败数,设备数,用户数,平均耗时(ms),最大耗时(ms)\n");
        for (AppBehaviorEventReportRowDTO row : report.rows()) {
            builder.append(escapeCsv(row.statDate())).append(',')
                    .append(escapeCsv(row.eventType())).append(',')
                    .append(escapeCsv(row.eventName())).append(',')
                    .append(row.totalCount()).append(',')
                    .append(row.successCount()).append(',')
                    .append(row.failureCount()).append(',')
                    .append(row.deviceCount()).append(',')
                    .append(row.userCount()).append(',')
                    .append(row.avgDurationMs() == null ? "" : row.avgDurationMs()).append(',')
                    .append(row.maxDurationMs() == null ? "" : row.maxDurationMs())
                    .append('\n');
        }
        return builder.toString();
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        String escaped = text.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private LocalDateTime parseDateTime(String raw, String fieldName) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace('T', ' ');
        try {
            return LocalDateTime.parse(normalized, SECOND_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // try other patterns
        }
        try {
            return LocalDateTime.parse(normalized, MINUTE_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // try other patterns
        }
        try {
            return LocalDateTime.parse(raw.trim(), ISO_MINUTE_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // try ISO default parser
        }
        try {
            return LocalDateTime.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " format must be yyyy-MM-ddTHH:mm or yyyy-MM-ddTHH:mm:ss");
        }
    }

    private String sanitizeFileToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return "APP";
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_\\-]", "_");
        if (normalized.isBlank()) {
            return "APP";
        }
        return normalized;
    }

    private String resolveOperator() {
        String username = adminRequestContext.currentAdminUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return "admin#" + adminRequestContext.requiredAdminId();
    }
}
