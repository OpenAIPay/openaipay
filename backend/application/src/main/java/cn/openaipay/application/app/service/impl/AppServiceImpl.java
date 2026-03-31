package cn.openaipay.application.app.service.impl;

import cn.openaipay.application.app.command.BindAppDeviceLoginUserCommand;
import cn.openaipay.application.app.command.ChangeAppVersionStatusCommand;
import cn.openaipay.application.app.command.CheckAppVersionCommand;
import cn.openaipay.application.app.command.CreateAppInfoCommand;
import cn.openaipay.application.app.command.CreateAppVersionCommand;
import cn.openaipay.application.app.command.GenerateAppBehaviorEventReportCommand;
import cn.openaipay.application.app.command.PublishAppIosPackageCommand;
import cn.openaipay.application.app.command.QueryAppBehaviorEventsCommand;
import cn.openaipay.application.app.command.RecordAppBehaviorEventCommand;
import cn.openaipay.application.app.command.RecordAppVisitCommand;
import cn.openaipay.application.app.command.SummarizeAppBehaviorEventsCommand;
import cn.openaipay.application.app.command.SubmitAppIosPackageReviewCommand;
import cn.openaipay.application.app.command.UpdateAppVersionCommand;
import cn.openaipay.application.app.command.UpdateAppSettingsCommand;
import cn.openaipay.application.app.command.UpsertAppDeviceCommand;
import cn.openaipay.application.app.dto.AppBehaviorEventDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventReportDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventReportRowDTO;
import cn.openaipay.application.app.dto.AppBehaviorEventStatsDTO;
import cn.openaipay.application.app.dto.AppDemoProvisioningConfigDTO;
import cn.openaipay.application.app.dto.AppBehaviorMetricItemDTO;
import cn.openaipay.application.app.dto.AppDeviceDTO;
import cn.openaipay.application.app.dto.AppInfoDTO;
import cn.openaipay.application.app.dto.AppIosPackageDTO;
import cn.openaipay.application.app.dto.AppLoginDeviceAccountWhitelistDTO;
import cn.openaipay.application.app.dto.AppVersionCheckDTO;
import cn.openaipay.application.app.dto.AppVersionDTO;
import cn.openaipay.application.app.dto.AppVisitRecordDTO;
import cn.openaipay.application.app.service.AppService;
import cn.openaipay.domain.app.model.AppBehaviorEvent;
import cn.openaipay.domain.app.model.AppBehaviorEventQuery;
import cn.openaipay.domain.app.model.AppBehaviorEventReportRow;
import cn.openaipay.domain.app.model.AppBehaviorEventStats;
import cn.openaipay.domain.app.model.AppClientType;
import cn.openaipay.domain.app.model.AppDevice;
import cn.openaipay.domain.app.model.AppDeviceStatus;
import cn.openaipay.domain.app.model.AppInfo;
import cn.openaipay.domain.app.model.AppIosPackage;
import cn.openaipay.domain.app.model.AppLoginDeviceAccountWhitelist;
import cn.openaipay.domain.app.model.AppBehaviorMetricItem;
import cn.openaipay.domain.app.model.AppStatus;
import cn.openaipay.domain.app.model.AppUpdatePromptFrequency;
import cn.openaipay.domain.app.model.AppUpdateType;
import cn.openaipay.domain.app.model.AppVersion;
import cn.openaipay.domain.app.model.AppVersionStatus;
import cn.openaipay.domain.app.model.AppVisitRecord;
import cn.openaipay.domain.app.repository.AppBehaviorEventRepository;
import cn.openaipay.domain.app.repository.AppDeviceRepository;
import cn.openaipay.domain.app.repository.AppInfoRepository;
import cn.openaipay.domain.app.repository.AppIosPackageRepository;
import cn.openaipay.domain.app.repository.AppLoginDeviceAccountWhitelistRepository;
import cn.openaipay.domain.app.repository.AppVersionRepository;
import cn.openaipay.domain.app.repository.AppVisitRecordRepository;
import cn.openaipay.domain.app.service.AppVersionCheckPlan;
import cn.openaipay.domain.app.service.AppVersionDomainService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * App 版本管理应用服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Service
public class AppServiceImpl implements AppService {

    /** IOS应用编码 */
    private static final String DEFAULT_IOS_APP_CODE = "OPENAIPAY_IOS";
    /** 访问信息 */
    private static final int DEFAULT_VISIT_LIMIT = 20;
    /** 最大访问信息 */
    private static final int MAX_VISIT_LIMIT = 100;
    /** 默认埋点明细查询条数。 */
    private static final int DEFAULT_BEHAVIOR_LIMIT = 50;
    /** 最大埋点明细查询条数。 */
    private static final int MAX_BEHAVIOR_LIMIT = 500;
    /** 默认埋点统计 Top 条数。 */
    private static final int DEFAULT_BEHAVIOR_TOP_LIMIT = 10;
    /** 最大埋点统计 Top 条数。 */
    private static final int MAX_BEHAVIOR_TOP_LIMIT = 50;
    /** 默认埋点报表条数。 */
    private static final int DEFAULT_BEHAVIOR_REPORT_LIMIT = 200;
    /** 最大埋点报表条数。 */
    private static final int MAX_BEHAVIOR_REPORT_LIMIT = 1000;
    /** 应用版本单号 */
    private static final Pattern APP_VERSION_NO_PATTERN = Pattern.compile("^\\d{2}\\.\\d{3,4}\\.\\d+$");
    /** 用户提示信息默认文案。 */
    private static final String DEFAULT_VERSION_PROMPT_MESSAGE = "为保障使用体验，请更新到最新版本后继续使用。";

    /** 应用信息 */
    private final AppInfoRepository appInfoRepository;
    /** 应用版本信息 */
    private final AppVersionRepository appVersionRepository;
    /** 应用IOS信息 */
    private final AppIosPackageRepository appIosPackageRepository;
    /** 应用设备信息 */
    private final AppDeviceRepository appDeviceRepository;
    /** 应用访问记录信息 */
    private final AppVisitRecordRepository appVisitRecordRepository;
    /** 应用行为埋点信息 */
    private final AppBehaviorEventRepository appBehaviorEventRepository;
    /** 登录设备白名单账号仓储。 */
    private final AppLoginDeviceAccountWhitelistRepository appLoginDeviceAccountWhitelistRepository;
    /** 应用版本域信息 */
    private final AppVersionDomainService appVersionDomainService;

    public AppServiceImpl(AppInfoRepository appInfoRepository,
                                     AppVersionRepository appVersionRepository,
                                     AppIosPackageRepository appIosPackageRepository,
                                     AppDeviceRepository appDeviceRepository,
                                     AppVisitRecordRepository appVisitRecordRepository,
                                     AppBehaviorEventRepository appBehaviorEventRepository,
                                     AppLoginDeviceAccountWhitelistRepository appLoginDeviceAccountWhitelistRepository,
                                     AppVersionDomainService appVersionDomainService) {
        this.appInfoRepository = appInfoRepository;
        this.appVersionRepository = appVersionRepository;
        this.appIosPackageRepository = appIosPackageRepository;
        this.appDeviceRepository = appDeviceRepository;
        this.appVisitRecordRepository = appVisitRecordRepository;
        this.appBehaviorEventRepository = appBehaviorEventRepository;
        this.appLoginDeviceAccountWhitelistRepository = appLoginDeviceAccountWhitelistRepository;
        this.appVersionDomainService = appVersionDomainService;
    }

    /**
     * 创建应用信息。
     */
    @Override
    @Transactional
    public AppInfoDTO createApp(CreateAppInfoCommand command) {
        String appCode = normalizeRequired(command.appCode(), "appCode");
        LocalDateTime now = LocalDateTime.now();
        AppInfo appInfo = appInfoRepository.findByAppCode(appCode)
                .map(existing -> {
                    existing.rename(command.appName(), now);
                    existing.changeStatus(AppStatus.ENABLED, now);
                    return existing;
                })
                .orElseGet(() -> AppInfo.create(appCode, command.appName(), now));
        return toDTO(appInfoRepository.save(appInfo));
    }

    /**
     * 查询应用信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppInfoDTO> listApps() {
        return appInfoRepository.findAll().stream()
                .sorted(Comparator.comparing(AppInfo::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .map(this::toDTO)
                .toList();
    }

    /**
     * 更新应用设置信息。
     */
    @Override
    @Transactional
    public AppInfoDTO updateAppSettings(UpdateAppSettingsCommand command) {
        String appCode = normalizeRequired(command.appCode(), "appCode");
        AppInfo appInfo = appInfoRepository.findByAppCode(appCode)
                .orElseThrow(() -> new NoSuchElementException("app not found: " + appCode));
        appInfo.changeVersionPromptEnabled(Boolean.TRUE.equals(command.versionPromptEnabled()), LocalDateTime.now());
        appInfo.changeDemoAutoLoginEnabled(Boolean.TRUE.equals(command.demoAutoLoginEnabled()), LocalDateTime.now());
        appInfo.changeLoginDeviceBindingCheckEnabled(Boolean.TRUE.equals(command.loginDeviceBindingCheckEnabled()), LocalDateTime.now());
        String nextDemoTemplateLoginId = command.demoTemplateLoginId() == null
                ? appInfo.getDemoTemplateLoginId()
                : normalizeOptional(command.demoTemplateLoginId());
        String nextDemoContactLoginId = command.demoContactLoginId() == null
                ? appInfo.getDemoContactLoginId()
                : normalizeOptional(command.demoContactLoginId());
        String nextDemoLoginPassword = command.demoLoginPassword() == null
                ? appInfo.getDemoLoginPassword()
                : normalizeOptional(command.demoLoginPassword());
        appInfo.changeDemoProvisioningConfig(
                nextDemoTemplateLoginId,
                nextDemoContactLoginId,
                nextDemoLoginPassword,
                LocalDateTime.now()
        );
        return toDTO(appInfoRepository.save(appInfo));
    }

    /**
     * 查询演示账号初始化配置。
     */
    @Override
    @Transactional(readOnly = true)
    public AppDemoProvisioningConfigDTO getDemoProvisioningConfig(String appCode) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        AppInfo appInfo = appInfoRepository.findByAppCode(normalizedAppCode)
                .orElseThrow(() -> new NoSuchElementException("app not found: " + normalizedAppCode));
        return new AppDemoProvisioningConfigDTO(
                normalizeOptional(appInfo.getDemoTemplateLoginId()),
                normalizeOptional(appInfo.getDemoContactLoginId()),
                normalizeOptional(appInfo.getDemoLoginPassword())
        );
    }

    /**
     * 查询版本信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppVersionDTO> listVersions(String appCode) {
        List<AppVersion> versions = appVersionRepository.listByAppCode(normalizeRequired(appCode, "appCode"), AppClientType.IOS_IPHONE);
        return versions.stream().map(this::toVersionDTO).toList();
    }

    /**
     * 创建版本信息。
     */
    @Override
    @Transactional
    public AppVersionDTO createVersion(CreateAppVersionCommand command) {
        String appCode = normalizeRequired(command.appCode(), "appCode");
        String versionCode = normalizeRequired(command.versionCode(), "versionCode");
        String appVersionNo = validateVersionNo(command.appVersionNo(), "appVersionNo");
        String minSupportedVersionNo = validateOptionalVersionNo(command.minSupportedVersionNo(), "minSupportedVersionNo");
        appInfoRepository.findByAppCode(appCode)
                .orElseThrow(() -> new NoSuchElementException("app not found: " + appCode));
        if (appVersionRepository.findByVersionCode(versionCode).isPresent()) {
            throw new IllegalArgumentException("versionCode already exists: " + command.versionCode());
        }
        AppUpdateType updateType = AppUpdateType.fromCode(command.updateType());
        AppUpdatePromptFrequency promptFrequency = updateType == AppUpdateType.FORCE
                ? AppUpdatePromptFrequency.ALWAYS
                : AppUpdatePromptFrequency.fromCode(command.updatePromptFrequency());
        LocalDateTime now = LocalDateTime.now();
        AppVersion version = AppVersion.create(
                versionCode,
                appCode,
                appVersionNo,
                updateType,
                promptFrequency,
                resolveUserPromptMessage(command.versionDescription()),
                command.publisherRemark(),
                command.releaseRegions(),
                command.targetedRegions(),
                minSupportedVersionNo,
                now
        );
        AppVersion savedVersion = appVersionRepository.save(version);
        upsertIosPackageDraft(
                savedVersion,
                command.iosCode(),
                command.appStoreUrl(),
                command.packageSizeBytes(),
                command.md5(),
                now
        );
        return toVersionDTO(savedVersion);
    }

    /**
     * 更新版本信息。
     */
    @Override
    @Transactional
    public AppVersionDTO updateVersion(UpdateAppVersionCommand command) {
        AppVersion version = mustGetVersion(command.versionCode());
        AppUpdateType updateType = AppUpdateType.fromCode(command.updateType());
        AppUpdatePromptFrequency promptFrequency = updateType == AppUpdateType.FORCE
                ? AppUpdatePromptFrequency.ALWAYS
                : AppUpdatePromptFrequency.fromCode(command.updatePromptFrequency());
        String minSupportedVersionNo = validateOptionalVersionNo(command.minSupportedVersionNo(), "minSupportedVersionNo");
        LocalDateTime now = LocalDateTime.now();
        version.refreshUpdatePolicy(
                updateType,
                promptFrequency,
                minSupportedVersionNo,
                resolveUserPromptMessage(command.versionDescription()),
                command.publisherRemark(),
                now
        );
        AppVersion savedVersion = appVersionRepository.save(version);
        upsertIosPackageDraft(
                savedVersion,
                command.iosCode(),
                command.appStoreUrl(),
                command.packageSizeBytes(),
                command.md5(),
                now
        );
        return toVersionDTO(savedVersion);
    }

    /**
     * 处理版本状态。
     */
    @Override
    @Transactional
    public AppVersionDTO changeVersionStatus(ChangeAppVersionStatusCommand command) {
        AppVersion version = mustGetVersion(command.versionCode());
        AppVersionStatus targetStatus = AppVersionStatus.fromCode(command.status());
        LocalDateTime now = LocalDateTime.now();
        version.changeStatus(targetStatus, now);
        if (targetStatus == AppVersionStatus.ENABLED) {
            List<AppVersion> allVersions = appVersionRepository.listByAppCode(version.getAppCode(), version.getClientType());
            appVersionDomainService.markLatestPublishedVersions(allVersions, version.getVersionCode(), now);
            for (AppVersion item : allVersions) {
                appVersionRepository.save(item);
            }
            return toVersionDTO(mustGetVersion(version.getVersionCode()));
        }
        if (targetStatus == AppVersionStatus.ARCHIVED || targetStatus == AppVersionStatus.DISABLED) {
            version.markLatestPublishedVersion(false, now);
        }
        return toVersionDTO(appVersionRepository.save(version));
    }

    /**
     * 提交IOS信息。
     */
    @Override
    @Transactional
    public AppVersionDTO submitIosPackageReview(SubmitAppIosPackageReviewCommand command) {
        AppVersion version = mustGetVersion(command.versionCode());
        AppIosPackage iosPackage = appIosPackageRepository.findByVersionCode(version.getVersionCode())
                .orElseThrow(() -> new NoSuchElementException("ios package not found for versionCode: " + version.getVersionCode()));
        iosPackage.submitReview(normalizeRequired(command.submittedBy(), "submittedBy"), LocalDateTime.now());
        appIosPackageRepository.save(iosPackage);
        return toVersionDTO(version);
    }

    /**
     * 发布IOS信息。
     */
    @Override
    @Transactional
    public AppVersionDTO publishIosPackage(PublishAppIosPackageCommand command) {
        AppVersion version = mustGetVersion(command.versionCode());
        AppIosPackage iosPackage = appIosPackageRepository.findByVersionCode(version.getVersionCode())
                .orElseThrow(() -> new NoSuchElementException("ios package not found for versionCode: " + version.getVersionCode()));
        LocalDateTime now = LocalDateTime.now();
        iosPackage.publish(now);
        appIosPackageRepository.save(iosPackage);
        List<AppVersion> allVersions = appVersionRepository.listByAppCode(version.getAppCode(), AppClientType.IOS_IPHONE);
        appVersionDomainService.markLatestPublishedVersions(allVersions, version.getVersionCode(), now);
        for (AppVersion item : allVersions) {
            appVersionRepository.save(item);
        }
        return toVersionDTO(mustGetVersion(version.getVersionCode()));
    }

    /**
     * 查询设备信息列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppDeviceDTO> listDevices(String appCode) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        List<AppVersion> versions = appVersionRepository.listByAppCode(normalizedAppCode, AppClientType.IOS_IPHONE);
        Map<Long, AppVersion> versionById = new HashMap<>();
        Map<Long, AppIosPackage> iosPackageById = new HashMap<>();
        for (AppVersion version : versions) {
            if (version.getId() != null) {
                versionById.put(version.getId(), version);
            }
            appIosPackageRepository.findByVersionCode(version.getVersionCode())
                    .ifPresent(pkg -> {
                        if (pkg.getId() != null) {
                            iosPackageById.put(pkg.getId(), pkg);
                        }
                    });
        }
        return appDeviceRepository.listByAppCode(normalizedAppCode).stream()
                .map(device -> toDeviceDTO(device, versionById.get(device.getCurrentAppVersionId()), iosPackageById.get(device.getCurrentIosPackageId())))
                .toList();
    }

    /**
     * 查询访问记录列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppVisitRecordDTO> listRecentVisitRecords(String deviceId, Integer limit) {
        return appVisitRecordRepository.listRecentByDeviceId(normalizeRequired(deviceId, "deviceId"), normalizeLimit(limit))
                .stream()
                .map(this::toVisitDTO)
                .toList();
    }

    /**
     * 保存或更新设备信息。
     */
    @Override
    @Transactional
    public AppDeviceDTO upsertDevice(UpsertAppDeviceCommand command) {
        String deviceId = normalizeRequired(command.deviceId(), "deviceId");
        String appCode = normalizeRequired(command.appCode(), "appCode");
        appInfoRepository.findByAppCode(appCode)
                .orElseThrow(() -> new NoSuchElementException("app not found: " + appCode));
        LocalDateTime now = LocalDateTime.now();
        AppDevice device = appDeviceRepository.findByDeviceId(deviceId)
                .orElseGet(() -> AppDevice.register(deviceId, appCode, command.clientIds(), command.deviceBrand(), command.osVersion(), now));
        device.refreshDeviceProfile(command.clientIds(), command.deviceBrand(), command.osVersion(), now);
        if (Boolean.TRUE.equals(command.started())) {
            device.touchStarted(now);
        } else {
            device.touchOpened(now);
        }
        bindDeviceVersion(device, appCode, command.currentVersionCode(), command.currentVersionNo(), now);
        AppDevice saved = appDeviceRepository.save(device);
        AppVersion version = saved.getCurrentAppVersionId() == null ? null : appVersionRepository.listByAppCode(saved.getAppCode(), AppClientType.IOS_IPHONE).stream()
                .filter(item -> item.getId() != null && item.getId().equals(saved.getCurrentAppVersionId()))
                .findFirst()
                .orElse(null);
        AppIosPackage iosPackage = version == null ? null : appIosPackageRepository.findByVersionCode(version.getVersionCode()).orElse(null);
        return toDeviceDTO(saved, version, iosPackage);
    }

    /**
     * 处理登录用户信息。
     */
    @Override
    @Transactional
    public AppDeviceDTO bindLoginUser(BindAppDeviceLoginUserCommand command) {
        String deviceId = normalizeRequired(command.deviceId(), "deviceId");
        LocalDateTime now = LocalDateTime.now();
        AppDevice device = appDeviceRepository.findByDeviceId(deviceId)
                .orElseGet(() -> AppDevice.register(deviceId, DEFAULT_IOS_APP_CODE, List.of(), "APPLE", null, now));
        device.touchOpened(now);
        device.bindLoginUser(
                command.userId(),
                command.aipayUid(),
                command.loginId(),
                command.accountStatus(),
                command.kycLevel(),
                command.nickname(),
                command.avatarUrl(),
                command.mobile(),
                command.maskedRealName(),
                command.idCardNoMasked(),
                command.countryCode(),
                command.gender(),
                command.region(),
                now
        );
        AppDevice saved = appDeviceRepository.save(device);
        AppVersion version = saved.getCurrentAppVersionId() == null ? null : appVersionRepository.listByAppCode(saved.getAppCode(), AppClientType.IOS_IPHONE).stream()
                .filter(item -> item.getId() != null && item.getId().equals(saved.getCurrentAppVersionId()))
                .findFirst()
                .orElse(null);
        AppIosPackage iosPackage = version == null ? null : appIosPackageRepository.findByVersionCode(version.getVersionCode()).orElse(null);
        return toDeviceDTO(saved, version, iosPackage);
    }

    /**
     * 查询账号绑定的设备ID。
     */
    @Override
    @Transactional(readOnly = true)
    public String queryBoundDeviceId(Long userId, String loginId) {
        Long normalizedUserId = normalizeUserId(userId);
        String normalizedLoginId = normalizeOptional(loginId);
        if (normalizedUserId == null && normalizedLoginId == null) {
            return null;
        }

        if (normalizedUserId != null) {
            String deviceId = appDeviceRepository.findLatestByUserId(normalizedUserId)
                    .map(AppDevice::getDeviceId)
                    .map(this::normalizeOptional)
                    .orElse(null);
            if (deviceId != null) {
                return deviceId;
            }
        }

        if (normalizedLoginId == null) {
            return null;
        }
        return appDeviceRepository.findLatestByLoginId(normalizedLoginId)
                .map(AppDevice::getDeviceId)
                .map(this::normalizeOptional)
                .orElse(null);
    }

    /**
     * 判断是否启用登录本机注册校验。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isLoginDeviceBindingCheckEnabled(String appCode) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        AppInfo appInfo = appInfoRepository.findByAppCode(normalizedAppCode).orElse(null);
        return appInfo == null || appInfo.isLoginDeviceBindingCheckEnabled();
    }

    /**
     * 判断账号是否允许在设备白名单登录。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isLoginWhitelistAllowed(String appCode, String deviceId, String loginId) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        String normalizedDeviceId = normalizeRequired(deviceId, "deviceId");
        String normalizedLoginId = normalizeRequired(loginId, "loginId");
        return appLoginDeviceAccountWhitelistRepository
                .existsEnabledByAppCodeAndDeviceIdAndLoginId(normalizedAppCode, normalizedDeviceId, normalizedLoginId);
    }

    /**
     * 判断账号是否配置登录白名单。
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isLoginWhitelistConfigured(String appCode, String loginId) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        String normalizedLoginId = normalizeRequired(loginId, "loginId");
        return appLoginDeviceAccountWhitelistRepository
                .existsEnabledByAppCodeAndLoginId(normalizedAppCode, normalizedLoginId);
    }

    /**
     * 查询设备登录白名单账号列表。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppLoginDeviceAccountWhitelistDTO> listLoginWhitelistAccounts(String appCode, String deviceId) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        String normalizedDeviceId = normalizeRequired(deviceId, "deviceId");
        return appLoginDeviceAccountWhitelistRepository
                .listEnabledByAppCodeAndDeviceId(normalizedAppCode, normalizedDeviceId)
                .stream()
                .map(this::toLoginWhitelistAccountDTO)
                .toList();
    }

    /**
     * 绑定登录设备白名单账号。
     */
    @Override
    @Transactional
    public void bindLoginWhitelistAccount(String appCode, String deviceId, String loginId, String nickname) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode");
        String normalizedDeviceId = normalizeRequired(deviceId, "deviceId");
        String normalizedLoginId = normalizeRequired(loginId, "loginId");
        LocalDateTime now = LocalDateTime.now();
        appLoginDeviceAccountWhitelistRepository.save(new AppLoginDeviceAccountWhitelist(
                null,
                normalizedAppCode,
                normalizedDeviceId,
                normalizedLoginId,
                normalizeOptional(nickname),
                true,
                now,
                now
        ));
    }

    /**
     * 记录访问信息。
     */
    @Override
    @Transactional
    public AppVisitRecordDTO recordVisit(RecordAppVisitCommand command) {
        String deviceId = normalizeRequired(command.deviceId(), "deviceId");
        String appCode = normalizeRequired(command.appCode(), "appCode");
        AppVersion version = resolveVersion(appCode, command.currentVersionCode(), command.currentVersionNo());
        AppVisitRecord record = AppVisitRecord.record(
                deviceId,
                appCode,
                command.clientId(),
                command.ipAddress(),
                command.locationInfo(),
                command.tenantCode(),
                command.networkType(),
                version == null ? null : version.getId(),
                command.deviceBrand(),
                command.osVersion(),
                command.apiName(),
                command.requestParamsText(),
                command.resultSummary(),
                command.durationMs(),
                LocalDateTime.now()
        );
        AppVisitRecord saved = appVisitRecordRepository.save(record);
        appDeviceRepository.findByDeviceId(deviceId).ifPresent(device -> {
            device.touchOpened(LocalDateTime.now());
            bindDeviceVersion(device, appCode, command.currentVersionCode(), command.currentVersionNo(), LocalDateTime.now());
            appDeviceRepository.save(device);
        });
        return toVisitDTO(saved);
    }

    /**
     * 记录行为埋点信息。
     */
    @Override
    @Transactional
    public AppBehaviorEventDTO recordBehaviorEvent(RecordAppBehaviorEventCommand command) {
        String deviceId = normalizeRequired(command.deviceId(), "deviceId");
        String appCode = normalizeRequired(command.appCode(), "appCode");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventOccurredAt = resolveEventOccurredAt(command.eventAtEpochMs(), now);
        String eventId = normalizeOptional(command.eventId());
        if (eventId == null) {
            eventId = "ev-" + UUID.randomUUID().toString().replace("-", "");
        }
        AppBehaviorEvent event = AppBehaviorEvent.record(
                eventId,
                command.sessionId(),
                appCode,
                command.eventName(),
                command.eventType(),
                command.eventCode(),
                command.pageName(),
                command.actionName(),
                command.resultStatus(),
                command.traceId(),
                deviceId,
                command.clientId(),
                command.userId(),
                command.aipayUid(),
                command.loginId(),
                command.accountStatus(),
                command.kycLevel(),
                command.nickname(),
                command.mobile(),
                command.ipAddress(),
                command.locationInfo(),
                command.tenantCode(),
                command.networkType(),
                command.appVersionNo(),
                command.appBuildNo(),
                command.deviceBrand(),
                command.deviceModel(),
                command.deviceName(),
                command.deviceType(),
                command.osName(),
                command.osVersion(),
                command.locale(),
                command.timezone(),
                command.language(),
                command.countryCode(),
                command.carrierName(),
                command.screenWidth(),
                command.screenHeight(),
                command.viewportWidth(),
                command.viewportHeight(),
                command.durationMs(),
                command.loginDurationMs(),
                eventOccurredAt,
                command.payloadJson()
        );
        AppBehaviorEvent saved = appBehaviorEventRepository.save(event);

        AppVersion version = resolveVersion(appCode, null, command.appVersionNo());
        AppDevice device = appDeviceRepository.findByDeviceId(deviceId)
                .orElseGet(() -> AppDevice.register(
                        deviceId,
                        appCode,
                        command.clientId() == null ? List.of() : List.of(command.clientId()),
                        command.deviceBrand(),
                        command.osVersion(),
                        now
                ));
        device.touchOpened(now);
        if (command.clientId() != null || command.deviceBrand() != null || command.osVersion() != null) {
            device.refreshDeviceProfile(
                    command.clientId() == null ? device.getClientIds() : List.of(command.clientId()),
                    command.deviceBrand() == null ? device.getDeviceBrand() : command.deviceBrand(),
                    command.osVersion() == null ? device.getOsVersion() : command.osVersion(),
                    now
            );
        }
        if (version != null) {
            AppIosPackage iosPackage = appIosPackageRepository.findByVersionCode(version.getVersionCode()).orElse(null);
            device.bindCurrentVersion(version.getId(), iosPackage == null ? null : iosPackage.getId(), now);
            device.changeStatus(AppDeviceStatus.ACTIVE, now);
        }
        if (command.userId() != null && command.userId() > 0) {
            device.bindLoginUser(
                    command.userId(),
                    command.aipayUid(),
                    command.loginId(),
                    command.accountStatus(),
                    command.kycLevel(),
                    command.nickname(),
                    null,
                    command.mobile(),
                    null,
                    null,
                    command.countryCode(),
                    null,
                    null,
                    now
            );
        }
        appDeviceRepository.save(device);
        return toBehaviorDTO(saved);
    }

    /**
     * 查询行为埋点明细。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppBehaviorEventDTO> listBehaviorEvents(QueryAppBehaviorEventsCommand command) {
        AppBehaviorEventQuery query = new AppBehaviorEventQuery(
                normalizeRequired(command.appCode(), "appCode"),
                normalizeOptional(command.eventType()),
                normalizeOptional(command.eventName()),
                normalizeOptional(command.pageName()),
                normalizeOptional(command.deviceId()),
                normalizeUserId(command.userId()),
                command.startAt(),
                command.endAt(),
                normalizeBehaviorLimit(command.limit())
        );
        validateBehaviorQueryTimeRange(query.startAt(), query.endAt());
        return appBehaviorEventRepository.listByQuery(query).stream().map(this::toBehaviorDTO).toList();
    }

    /**
     * 聚合行为埋点统计数据。
     */
    @Override
    @Transactional(readOnly = true)
    public AppBehaviorEventStatsDTO summarizeBehaviorEvents(SummarizeAppBehaviorEventsCommand command) {
        AppBehaviorEventQuery query = new AppBehaviorEventQuery(
                normalizeRequired(command.appCode(), "appCode"),
                normalizeOptional(command.eventType()),
                normalizeOptional(command.eventName()),
                normalizeOptional(command.pageName()),
                normalizeOptional(command.deviceId()),
                normalizeUserId(command.userId()),
                command.startAt(),
                command.endAt(),
                null
        );
        validateBehaviorQueryTimeRange(query.startAt(), query.endAt());
        AppBehaviorEventStats stats = appBehaviorEventRepository.summarize(query, normalizeBehaviorTopLimit(command.topLimit()));
        return toBehaviorStatsDTO(stats);
    }

    /**
     * 生成行为埋点报表。
     */
    @Override
    @Transactional(readOnly = true)
    public AppBehaviorEventReportDTO generateBehaviorEventReport(GenerateAppBehaviorEventReportCommand command) {
        AppBehaviorEventQuery query = new AppBehaviorEventQuery(
                normalizeRequired(command.appCode(), "appCode"),
                normalizeOptional(command.eventType()),
                normalizeOptional(command.eventName()),
                normalizeOptional(command.pageName()),
                normalizeOptional(command.deviceId()),
                normalizeUserId(command.userId()),
                command.startAt(),
                command.endAt(),
                normalizeBehaviorReportLimit(command.limit())
        );
        validateBehaviorQueryTimeRange(query.startAt(), query.endAt());
        List<AppBehaviorEventReportRowDTO> rows = appBehaviorEventRepository.listReportRows(query).stream()
                .map(this::toBehaviorReportRowDTO)
                .toList();
        return new AppBehaviorEventReportDTO(
                query.appCode(),
                query.startAt(),
                query.endAt(),
                LocalDateTime.now(),
                rows
        );
    }

    /**
     * 校验版本信息。
     */
    @Override
    @Transactional(readOnly = true)
    public AppVersionCheckDTO checkVersion(CheckAppVersionCommand command) {
        String appCode = normalizeRequired(command.appCode(), "appCode");
        String currentVersionNo = normalizeOptional(command.currentVersionNo());
        AppInfo appInfo = appInfoRepository.findByAppCode(appCode).orElse(null);
        AppVersion latestVersion = appVersionRepository.findLatestPublishedVersion(appCode, AppClientType.IOS_IPHONE)
                .orElse(null);
        AppVersionCheckPlan plan = appVersionDomainService.planVersionCheck(appInfo, latestVersion, currentVersionNo);
        AppIosPackage iosPackage = latestVersion == null
                ? null
                : appIosPackageRepository.findByVersionCode(latestVersion.getVersionCode()).orElse(null);
        boolean demoAutoLoginEnabled = appInfo == null || appInfo.isDemoAutoLoginEnabled();
        return new AppVersionCheckDTO(
                appCode,
                currentVersionNo,
                plan.versionPromptEnabled(),
                demoAutoLoginEnabled,
                plan.latestVersionCode(),
                plan.latestVersionNo(),
                plan.updateAvailable(),
                plan.forceUpdate(),
                plan.updateType(),
                plan.updatePromptFrequency(),
                plan.versionDescription(),
                plan.minSupportedVersionNo(),
                iosPackage == null ? null : iosPackage.getAppStoreUrl(),
                iosPackage == null ? null : iosPackage.getPackageSizeBytes(),
                iosPackage == null ? null : iosPackage.getMd5(),
                iosPackage == null ? null : iosPackage.getReleaseStatus().name()
        );
    }

    private void bindDeviceVersion(AppDevice device, String appCode, String currentVersionCode, String currentVersionNo, LocalDateTime now) {
        AppVersion version = resolveVersion(appCode, currentVersionCode, currentVersionNo);
        if (version == null) {
            return;
        }
        AppIosPackage iosPackage = appIosPackageRepository.findByVersionCode(version.getVersionCode()).orElse(null);
        device.bindCurrentVersion(version.getId(), iosPackage == null ? null : iosPackage.getId(), now);
        device.changeStatus(AppDeviceStatus.ACTIVE, now);
    }

    private AppVersion resolveVersion(String appCode, String currentVersionCode, String currentVersionNo) {
        String normalizedVersionCode = normalizeOptional(currentVersionCode);
        if (normalizedVersionCode != null) {
            AppVersion version = appVersionRepository.findByVersionCode(normalizedVersionCode).orElse(null);
            if (version != null) {
                return version;
            }
        }

        String normalizedVersionNo = normalizeOptional(currentVersionNo);
        if (normalizedVersionNo == null) {
            return null;
        }

        return appVersionRepository.listByAppCode(normalizeRequired(appCode, "appCode"), AppClientType.IOS_IPHONE).stream()
                .filter(item -> normalizedVersionNo.equals(item.getAppVersionNo()))
                .findFirst()
                .orElse(null);
    }

    private void upsertIosPackageDraft(AppVersion version,
                                       String iosCode,
                                       String appStoreUrl,
                                       Long packageSizeBytes,
                                       String md5,
                                       LocalDateTime now) {
        String normalizedIosCode = normalizeOptional(iosCode);
        String normalizedAppStoreUrl = normalizeOptional(appStoreUrl);
        String normalizedMd5 = normalizeOptional(md5);
        boolean hasIosDraftInput = normalizedIosCode != null
                || normalizedAppStoreUrl != null
                || packageSizeBytes != null
                || normalizedMd5 != null;
        AppIosPackage existingPackage = appIosPackageRepository.findByVersionCode(version.getVersionCode()).orElse(null);
        if (!hasIosDraftInput && existingPackage == null) {
            return;
        }
        if (existingPackage != null) {
            existingPackage.refreshDraft(normalizedAppStoreUrl, packageSizeBytes, normalizedMd5, now);
            appIosPackageRepository.save(existingPackage);
            return;
        }
        String resolvedIosCode = normalizedIosCode != null
                ? normalizedIosCode
                : buildDefaultIosCode(version.getAppCode(), version.getAppVersionNo());
        AppIosPackage iosPackage = AppIosPackage.create(
                resolvedIosCode,
                version.getAppCode(),
                version.getVersionCode(),
                normalizedAppStoreUrl,
                packageSizeBytes,
                normalizedMd5,
                now
        );
        appIosPackageRepository.save(iosPackage);
    }

    private String buildDefaultIosCode(String appCode, String appVersionNo) {
        String normalizedAppCode = normalizeRequired(appCode, "appCode").toUpperCase(Locale.ROOT);
        String normalizedVersionNo = validateVersionNo(appVersionNo, "appVersionNo");
        return normalizedAppCode + "_PKG_" + normalizedVersionNo.replace('.', '_');
    }

    private AppVersion mustGetVersion(String versionCode) {
        return appVersionRepository.findByVersionCode(normalizeRequired(versionCode, "versionCode"))
                .orElseThrow(() -> new NoSuchElementException("app version not found: " + versionCode));
    }

    private AppInfoDTO toDTO(AppInfo appInfo) {
        String demoPassword = normalizeOptional(appInfo.getDemoLoginPassword());
        return new AppInfoDTO(
                appInfo.getAppCode(),
                appInfo.getAppName(),
                appInfo.getStatus().name(),
                appInfo.isVersionPromptEnabled(),
                appInfo.isDemoAutoLoginEnabled(),
                appInfo.isLoginDeviceBindingCheckEnabled(),
                normalizeOptional(appInfo.getDemoTemplateLoginId()),
                normalizeOptional(appInfo.getDemoContactLoginId()),
                demoPassword != null,
                demoPassword == null ? null : maskSecret(demoPassword),
                appInfo.getCreatedAt(),
                appInfo.getUpdatedAt()
        );
    }

    private String maskSecret(String secret) {
        String normalized = normalizeOptional(secret);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() <= 2) {
            return "*".repeat(normalized.length());
        }
        return normalized.substring(0, 1)
                + "*".repeat(Math.max(1, normalized.length() - 2))
                + normalized.substring(normalized.length() - 1);
    }

    private AppVersionDTO toVersionDTO(AppVersion version) {
        AppIosPackage iosPackage = appIosPackageRepository.findByVersionCode(version.getVersionCode()).orElse(null);
        return new AppVersionDTO(
                version.getVersionCode(),
                version.getAppCode(),
                version.getClientType().name(),
                version.getAppVersionNo(),
                version.getUpdateType().name(),
                version.getUpdatePromptFrequency().name(),
                version.getVersionDescription(),
                version.getPublisherRemark(),
                version.getReleaseRegions(),
                version.getTargetedRegions(),
                version.getMinSupportedVersionNo(),
                version.isLatestPublishedVersion(),
                version.getStatus().name(),
                toIosPackageDTO(iosPackage),
                version.getCreatedAt(),
                version.getUpdatedAt()
        );
    }

    private AppIosPackageDTO toIosPackageDTO(AppIosPackage iosPackage) {
        if (iosPackage == null) {
            return null;
        }
        return new AppIosPackageDTO(
                iosPackage.getIosCode(),
                iosPackage.getAppCode(),
                iosPackage.getVersionCode(),
                iosPackage.getAppStoreUrl(),
                iosPackage.getPackageSizeBytes(),
                iosPackage.getMd5(),
                iosPackage.getReviewSubmittedAt(),
                iosPackage.getPublishedAt(),
                iosPackage.getReleaseStatus().name(),
                iosPackage.getReviewSubmittedBy(),
                iosPackage.getCreatedAt(),
                iosPackage.getUpdatedAt()
        );
    }

    private AppDeviceDTO toDeviceDTO(AppDevice device, AppVersion version, AppIosPackage iosPackage) {
        return new AppDeviceDTO(
                device.getDeviceId(),
                device.getAppCode(),
                device.getClientIds(),
                device.getStatus().name(),
                device.getInstalledAt(),
                device.getStartedAt(),
                device.getLastOpenedAt(),
                device.getCurrentAppVersionId(),
                version == null ? null : version.getVersionCode(),
                version == null ? null : version.getAppVersionNo(),
                device.getCurrentIosPackageId(),
                iosPackage == null ? null : iosPackage.getIosCode(),
                device.getAppUpdatedAt(),
                device.getDeviceBrand(),
                device.getOsVersion(),
                device.getUserId(),
                device.getAipayUid(),
                device.getLoginId(),
                device.getAccountStatus(),
                device.getKycLevel(),
                device.getNickname(),
                device.getAvatarUrl(),
                device.getMobile(),
                device.getMaskedRealName(),
                device.getIdCardNoMasked(),
                device.getCountryCode(),
                device.getGender(),
                device.getRegion(),
                device.getLastLoginAt(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    private AppLoginDeviceAccountWhitelistDTO toLoginWhitelistAccountDTO(AppLoginDeviceAccountWhitelist account) {
        String normalizedNickname = normalizeOptional(account.getNickname());
        return new AppLoginDeviceAccountWhitelistDTO(
                account.getLoginId(),
                normalizedNickname == null ? account.getLoginId() : normalizedNickname
        );
    }

    private AppVisitRecordDTO toVisitDTO(AppVisitRecord record) {
        return new AppVisitRecordDTO(
                record.getId(),
                record.getDeviceId(),
                record.getAppCode(),
                record.getClientId(),
                record.getIpAddress(),
                record.getLocationInfo(),
                record.getTenantCode(),
                record.getClientType().name(),
                record.getNetworkType(),
                record.getAppVersionId(),
                record.getDeviceBrand(),
                record.getOsVersion(),
                record.getApiName(),
                record.getRequestParamsText(),
                record.getCalledAt(),
                record.getResultSummary(),
                record.getDurationMs()
        );
    }

    private AppBehaviorEventDTO toBehaviorDTO(AppBehaviorEvent event) {
        return new AppBehaviorEventDTO(
                event.getId(),
                event.getEventId(),
                event.getSessionId(),
                event.getAppCode(),
                event.getEventName(),
                event.getEventType(),
                event.getEventCode(),
                event.getPageName(),
                event.getActionName(),
                event.getResultStatus(),
                event.getTraceId(),
                event.getDeviceId(),
                event.getClientId(),
                event.getUserId(),
                event.getAipayUid(),
                event.getLoginId(),
                event.getAccountStatus(),
                event.getKycLevel(),
                event.getNickname(),
                event.getMobile(),
                event.getIpAddress(),
                event.getLocationInfo(),
                event.getTenantCode(),
                event.getNetworkType(),
                event.getAppVersionNo(),
                event.getAppBuildNo(),
                event.getDeviceBrand(),
                event.getDeviceModel(),
                event.getDeviceName(),
                event.getDeviceType(),
                event.getOsName(),
                event.getOsVersion(),
                event.getLocale(),
                event.getTimezone(),
                event.getLanguage(),
                event.getCountryCode(),
                event.getCarrierName(),
                event.getScreenWidth(),
                event.getScreenHeight(),
                event.getViewportWidth(),
                event.getViewportHeight(),
                event.getDurationMs(),
                event.getLoginDurationMs(),
                event.getEventOccurredAt(),
                event.getPayloadJson(),
                event.getCreatedAt()
        );
    }

    private AppBehaviorEventStatsDTO toBehaviorStatsDTO(AppBehaviorEventStats stats) {
        return new AppBehaviorEventStatsDTO(
                stats.totalCount(),
                stats.uniqueDeviceCount(),
                stats.uniqueUserCount(),
                stats.successCount(),
                stats.failureCount(),
                stats.avgDurationMs() == null ? BigDecimal.ZERO : stats.avgDurationMs(),
                stats.firstOccurredAt(),
                stats.lastOccurredAt(),
                stats.eventTypeDistribution().stream().map(this::toBehaviorMetricItemDTO).toList(),
                stats.topEventDistribution().stream().map(this::toBehaviorMetricItemDTO).toList()
        );
    }

    private AppBehaviorMetricItemDTO toBehaviorMetricItemDTO(AppBehaviorMetricItem item) {
        return new AppBehaviorMetricItemDTO(item.key(), item.count());
    }

    private AppBehaviorEventReportRowDTO toBehaviorReportRowDTO(AppBehaviorEventReportRow row) {
        return new AppBehaviorEventReportRowDTO(
                row.statDate(),
                row.eventType(),
                row.eventName(),
                row.totalCount(),
                row.successCount(),
                row.failureCount(),
                row.deviceCount(),
                row.userCount(),
                row.avgDurationMs(),
                row.maxDurationMs()
        );
    }

    private Long normalizeUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userId;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_VISIT_LIMIT;
        }
        return Math.min(limit, MAX_VISIT_LIMIT);
    }

    private int normalizeBehaviorLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_BEHAVIOR_LIMIT;
        }
        return Math.min(limit, MAX_BEHAVIOR_LIMIT);
    }

    private int normalizeBehaviorTopLimit(Integer topLimit) {
        if (topLimit == null || topLimit <= 0) {
            return DEFAULT_BEHAVIOR_TOP_LIMIT;
        }
        return Math.min(topLimit, MAX_BEHAVIOR_TOP_LIMIT);
    }

    private int normalizeBehaviorReportLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_BEHAVIOR_REPORT_LIMIT;
        }
        return Math.min(limit, MAX_BEHAVIOR_REPORT_LIMIT);
    }

    private void validateBehaviorQueryTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("startAt must not be after endAt");
        }
    }

    private LocalDateTime resolveEventOccurredAt(Long eventAtEpochMs, LocalDateTime fallback) {
        if (eventAtEpochMs == null || eventAtEpochMs <= 0) {
            return fallback;
        }
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(eventAtEpochMs), ZoneId.systemDefault());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveUserPromptMessage(String rawMessage) {
        String normalizedMessage = normalizeOptional(rawMessage);
        return normalizedMessage == null ? DEFAULT_VERSION_PROMPT_MESSAGE : normalizedMessage;
    }

    private String validateVersionNo(String raw, String fieldName) {
        String normalized = normalizeRequired(raw, fieldName);
        if (!APP_VERSION_NO_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(fieldName + " format must be YY.MMDD.SEQUENCE, for example 26.315.1");
        }
        return normalized;
    }

    private String validateOptionalVersionNo(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return null;
        }
        if (!APP_VERSION_NO_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(fieldName + " format must be YY.MMDD.SEQUENCE, for example 26.315.1");
        }
        return normalized;
    }
}
